package ch.andre601.advancedserverlist.core.profiles.handlers.players.storage;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.PlayerHandler;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;

public class JSONPlayerHandler extends PlayerHandler{
    private final Type listType = new TypeToken<CopyOnWriteArrayList<CachedPlayer>>(){}.getType();
    private final Gson gson = new Gson();
    
    private final Path cacheFile;
    
    public JSONPlayerHandler(AdvancedServerList<?> core){
        super(core);
        this.cacheFile = core.plugin().folderPath().resolve("playercache.json");
    }
    
    @Override
    public void load(){
        logger().info("Loading Cached players from playercache.json file...");
        
        if(!Files.exists(cacheFile)){
            logger().info("No playercache.json file present. Skipping loading it...");
            return;
        }
        
        if(core().fileHandler().getBoolean("disableCache")){
            logger().info("disableCache enabled. Skipping player loading...");
            return;
        }
        
        try(BufferedReader reader = Files.newBufferedReader(cacheFile)){
            players = gson.fromJson(reader, listType);
        }catch(IOException ex){
            logger().warn("Encountered IOException while parsing playercache.json!", ex);
            return;
        }catch(JsonIOException | JsonSyntaxException ex){
            logger().warn("Encountered Json Exception while parsing playercache.json!", ex);
            return;
        }
        
        if(players == null){
            logger().warn("Unable to load playercache.json! Retrieved list was null.");
            players = new CopyOnWriteArrayList<>();
            return;
        }
        
        logger().success("Successfully loaded <white>%d</white> player(s) into Cache!", players.size());
    }
    
    @Override
    public void save(){
        logger().info("[-] Saving <white>playercache.json</white> file...");
        
        if(core().fileHandler().getBoolean("disableCache")){
            logger().debug(JSONPlayerHandler.class, "[-] Caching is disabled. Skipping player saving...");
            return;
        }
        
        try(BufferedWriter writer = Files.newBufferedWriter(cacheFile, StandardCharsets.UTF_8)){
            gson.toJson(players, listType, writer);
            logger().success("Successfully saved <white>playercache.json</white> file!");
        }catch(IOException | JsonIOException ex){
            logger().failure("Encountered an IOException while saving playercache.json file!", ex);
        }
    }
}
