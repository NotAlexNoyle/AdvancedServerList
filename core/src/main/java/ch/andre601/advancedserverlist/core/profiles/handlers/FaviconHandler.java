package ch.andre601.advancedserverlist.core.profiles.handlers;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FaviconHandler<F>{
    
    private final AdvancedServerList<F> core;
    private final PluginLogger logger;
    private final ThreadPoolExecutor faviconThreadPool;
    private final Cache<String, CompletableFuture<F>> faviconCache;
    
    private final Random random = new Random();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Map<String, FaviconPair<F>> localFavicons = new HashMap<>();
    
    public FaviconHandler(AdvancedServerList<F> core){
        this.core = core;
        this.logger = core.plugin().pluginLogger();
        this.faviconThreadPool = createThreadPool();
        this.faviconCache = CacheBuilder.newBuilder()
                .expireAfterWrite(core.fileHandler().getLong(1, 1, "faviconCacheTime"), TimeUnit.MINUTES)
                .build();
        
        loadLocalFavicons();
    }
    
    public F favicon(String input){
        if(localFavicons.containsKey(input.toLowerCase(Locale.ROOT))){
            logger.debug(FaviconHandler.class, "Returning local Favicon '%s'.", input);
            
            return localFavicons.get(input.toLowerCase(Locale.ROOT)).favicon();
        }
        
        if(input.equalsIgnoreCase("random")){
            logger.debug(FaviconHandler.class, "Returning random local Favicon.");
            
            FaviconPair<F> pair = randomFavicon();
            if(pair == null){
                logger.warn("Cannot return random Favicon. Make sure PNGs are available in the Favicons folder!");
                return null;
            }
            
            return pair.favicon();
        }
        
        CompletableFuture<F> favicon = faviconCache.getIfPresent(input);
        if(favicon != null)
            return favicon.getNow(null);
        
        logger.debug(FaviconHandler.class, "Processing '%s'", input);
        if(input.contains(";")){
            String[] parts = input.split(";");
            logger.debug(FaviconHandler.class, "Handling multi-part Favicon: ['%s']", String.join("', '", parts));
            favicon = CompletableFuture.supplyAsync(() -> {
                List<BufferedImage> images = new ArrayList<>();
                Arrays.stream(parts).forEach(part -> {
                    logger.debug(FaviconHandler.class, "Creating BufferedImage for part '%s'...", part);
                    images.add(create(part));
                });
                
                if(images.isEmpty()){
                    logger.debugWarn(FaviconHandler.class, "Unable to create Multi-part Favicon. List was empty.");
                    return null;
                }
                
                return composeFavicon(images);
            }, faviconThreadPool);
        }else{
            logger.debug(FaviconHandler.class, "Handling Favicon input '%s'...", input);
            
            favicon = CompletableFuture.supplyAsync(() -> {
                logger.debug(FaviconHandler.class, "Creating BufferedImage for '%s'...", input);
                
                BufferedImage image = create(input);
                if(image == null){
                    logger.warn("Cannot create Favicon for '%s'. BufferedImage was null!", input);
                    return null;
                }
                
                return createFavicon(image);
            }, faviconThreadPool);
        }
        
        faviconCache.put(input, favicon);
        return favicon.getNow(null);
    }
    
    public void cleanCache(){
        localFavicons.clear();
        faviconCache.invalidateAll();
        loadLocalFavicons();
    }
    
    private F composeFavicon(List<BufferedImage> images){
        logger.debug(FaviconHandler.class, "Composing Favicon out of %d part(s)...", images.size());
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        
        String strategy = core.fileHandler().getString("resize", "faviconStrategy");
        int layer = 0;
        for(BufferedImage img : images){
            layer++;
            logger.debug(FaviconHandler.class, "Applying layer %d...", layer);
            if(img == null){
                logger.debugWarn(FaviconHandler.class, "Layer %d was null! Skipping...", layer);
                continue;
            }
            
            switch(strategy.toLowerCase(Locale.ROOT)){
                case "center" -> {
                    int height = img.getHeight();
                    int width = img.getWidth();
                    
                    int posHeight = 0;
                    int posWidth = 0;
                    if(height != 64){
                        posHeight = (64 - height) / 2;
                    }
                    
                    // 64 - 128 = -64
                    
                    if(width != 64){
                        posWidth = (64 - width) / 2;
                    }
                    
                    graphics.drawImage(img, posWidth, posHeight, img.getWidth(), img.getHeight(), null);
                }
                case "none" -> graphics.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
                default -> graphics.drawImage(img, 0, 0, 64, 64, null);
            }
            
            logger.debug(FaviconHandler.class, "Applied layer %d!", layer);
        }
        
        graphics.dispose();
        
        logger.debug(FaviconHandler.class, "Image Composing complete! Creating Favicon instance...");
        return createFavicon(image);
    }
    
    private BufferedImage create(String input){
        if(input.toLowerCase(Locale.ROOT).startsWith("https://")){
            logger.debug(FaviconHandler.class, "Handling URL '%s'...", input);
            return fromURL(input);
        }else
        if(input.equalsIgnoreCase("random")){
            logger.debug(FaviconHandler.class, "Returning random local Favicon...");
            FaviconPair<F> pair = randomFavicon();
            return pair == null ? null : pair.image();
        }else
        if(input.toLowerCase(Locale.ROOT).endsWith(".png")){
            logger.debug(FaviconHandler.class, "Returning local Favicon '%s'...", input);
            FaviconPair<F> pair = localFavicons.get(input.toLowerCase(Locale.ROOT));
            return pair == null ? null : pair.image();
        }else{
            logger.debug(FaviconHandler.class, "Handling Player Name/UUID '%s'...", input);
            return fromURL("https://mc-heads.net/avatar/" + input + "/64");
        }
    }
    
    private FaviconPair<F> randomFavicon(){
        if(localFavicons.isEmpty())
            return null;
        
        List<FaviconPair<F>> favicons = new ArrayList<>(localFavicons.values());
        if(favicons.isEmpty())
            return null;
        
        if(favicons.size() == 1)
            return favicons.getFirst();
        
        synchronized(random){
            return favicons.get(random.nextInt(favicons.size()));
        }
    }
    
    private void loadLocalFavicons(){
        this.localFavicons.clear();
        
        logger.info("[-] Loading local images as Favicons...");
        
        Path folder = core.plugin().folderPath().resolve("favicons");
        if(!Files.exists(folder)){
            try{
                Files.createDirectories(folder);
                logger.debugSuccess(FaviconHandler.class, "Created favicons folder");
            }catch(IOException ex){
                logger.warn("Encountered IOException while creating favicons folder.", ex);
                return;
            }
        }
        
        try(Stream<Path> stream = Files.list(folder)){
            stream.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".png"))
                    .forEach(this::loadFile);
            
            logger.success("Loaded <white>%s</white> local Favicon(s).", localFavicons.size());
        }catch(IOException ex){
            logger.warn("Encountered IOException while loading local favicons.", ex);
        }
    }
    
    private void loadFile(Path path){
        try(InputStream stream = Files.newInputStream(path)){
            BufferedImage image = createBufferedImage(stream);
            if(image == null)
                return;
            
            F favicon = core.plugin().createFavicon(image);
            if(favicon == null)
                return;
            
            localFavicons.put(path.getFileName().toString().toLowerCase(Locale.ROOT), new FaviconPair<>(image, favicon));
        }catch(IOException ex){
            logger.warn("Encountered IOException while creating a Favicon from '%s'.", ex, path.getFileName().toString());
        }catch(Exception ex){
            logger.warn("Encountered an Exceotion while creating a Favicon from '%s'.", ex, path.getFileName().toString());
        }
    }
    
    private BufferedImage fromURL(String url){
        try{
            logger.debug(FaviconHandler.class, "Creating request for URL '%s'...", url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "AdvancedServerList-" + core.plugin().loader() + "/" + core.version())
                    .build();
            
            try(InputStream stream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body()){
                return createBufferedImage(stream);
            }catch(IOException ex){
                logger.warn("Encountered IOException while performing a web request for '%s'.", ex, url);
                return null;
            }catch(InterruptedException ex){
                logger.warn("Encountered an InterruptedException while performing a web request for '%s'.", ex, url);
                return null;
            }
        }catch(URISyntaxException ex){
            logger.warn("Received URISyntaxException for URL '%s'", ex, url);
            return null;
        }
    }
    
    private BufferedImage createBufferedImage(InputStream stream){
        logger.debug(FaviconHandler.class, "Creating BufferedImage from InputStream...");
        try{
            BufferedImage original = ImageIO.read(stream);
            if(original == null){
                logger.warn("Cannot create Favicon. Received a null BufferedImage!");
                return null;
            }
            
            logger.debug(FaviconHandler.class, "Check image size for possible resizing...");
            if(original.getHeight() == 64 && original.getWidth() == 64){
                logger.debug(FaviconHandler.class, "Image is 64x64. No resizing needed!");
                return original;
            }
            
            logger.debug(FaviconHandler.class, "Applying Favicon resize strategy...");
            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            
            String resizeMode = core.fileHandler().getString("resize", "faviconStrategy");
            switch(resizeMode.toLowerCase(Locale.ROOT)){
                case "center" -> {
                    int height = original.getHeight();
                    int width = original.getWidth();
                    
                    int posHeight = 0;
                    int posWidth = 0;
                    if(height != 64){
                        posHeight = (64 - height) / 2;
                    }
                    
                    if(width != 64){
                        posWidth = (64 - width) / 2;
                    }
                    
                    graphics.drawImage(original, posWidth, posHeight, original.getWidth(), original.getHeight(), null);
                }
                case "none" -> graphics.drawImage(original, 0, 0, original.getWidth(), original.getHeight(), null);
                default -> graphics.drawImage(original, 0, 0, 64, 64, null);
            }
            
            graphics.dispose();
            
            logger.debug(FaviconHandler.class, "Image resized. Returning it...");
            return image;
        }catch(IOException ex){
            logger.warn("Encountered an IOException while processing Favicon!", ex);
            return null;
        }
    }
    
    private F createFavicon(BufferedImage image){
        try{
            return core.plugin().createFavicon(image);
        }catch(Exception ex){
            logger.warn("Encountered an Exception while trying to create a Favicon instance.", ex);
            return null;
        }
    }
    
    private ThreadPoolExecutor createThreadPool(){
        return new ThreadPoolExecutor(3, 3, 60L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1024), r -> {
            Thread t = new Thread(r, "AdvancedServerList-FaviconThread");
            t.setDaemon(true);
            
            return t;
        });
    }
    
    private record FaviconPair<F>(BufferedImage image, F favicon){}
}
