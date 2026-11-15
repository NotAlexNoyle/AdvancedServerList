/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Andre_601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ch.andre601.advancedserverlist.core.check;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.objects.ValueCache;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unascribed.flexver.FlexVerComparator;
import io.leangen.geantyref.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateChecker{
    
    private final Type listType = new TypeToken<ArrayList<ModrinthVersion>>(){}.getType();
    private final Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setPrettyPrinting()
        .create();
    
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new UpdateCheckThread());
    
    private final ValueCache<CompletableFuture<ModrinthVersion>> cache = new ValueCache<>(Duration.ofMinutes(5));
    
    private final AdvancedServerList<?> core;
    private final PluginLogger logger;
    private final String loader;
    private final HttpClient client;
    
    public UpdateChecker(AdvancedServerList<?> core){
        this.core = core;
        this.logger = core.plugin().pluginLogger();
        this.loader = core.plugin().loader();
        this.client = HttpClient.newBuilder().executor(Executors.newSingleThreadExecutor(new UpdateCheckThread())).build();
        
        startUpdateChecker();
    }
    
    public void performCachedUpdateCheck(CmdSender sender){
        CompletableFuture<ModrinthVersion> modrinthVersion = cache.get(this::performUpdateCheck);
        
        modrinthVersion.whenComplete((version, throwable) -> {
            if(throwable != null){
                logger.warn("Encountered an Exception while checking for an update!", throwable);
                sender.sendErrorMsg("There was an exception while checking for an update. Please check the console for details.");
                return;
            }
            
            if(version == null)
                return;
            
            int result = version.compare(core.versions().getOrDefault("plugin", "-1"));
            if(result == -1){
                sender.sendPrefixedMsg("<green>A new Version of AdvancedServerList is available!");
                sender.sendPrefixedMsg("<green>Your version: <white>%s", core.versions().getOrDefault("plugin", "-1"));
                sender.sendPrefixedMsg("<green>Version on Modrinth: <white>%s", version.versionNumber());
                sender.sendPrefixedMsg(
                    "<click:open_url:'https://modrinth.com/plugin/advancedserverlist/version/%s'>" +
                    "<green>[<white>Download Page</white>]" +
                    "</click>",
                    version.id()
                );
            }else
            if(result == 1){
                sender.sendPrefixedMsg(
                    "Your version (<white>%</white>) is newer than what is available on Modrinth (<white>%s</white>).",
                    core.versions().getOrDefault("plugin", "-1"),
                    version.versionNumber()
                );
                sender.sendPrefixedMsg("Are you using a Development Version?");
            }
        });
    }
    
    public void disable(){
        executor.shutdown();
        try{
            if(!executor.awaitTermination(1, TimeUnit.SECONDS)){
                executor.shutdownNow();
                if(!executor.awaitTermination(1, TimeUnit.SECONDS))
                    logger.warn("Scheduler did not terminate in time!");
            }
        }catch(InterruptedException ex){
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void startUpdateChecker(){
        executor.scheduleAtFixedRate(() -> {
            logger.info("[-] Looking for an update...");
            
            performUpdateCheck().whenComplete((version, throwable) -> {
                if(version == null || throwable != null){
                    logger.failure("Failed to look for any updates. Check previous messages for causes.");
                    return;
                }
                
                logger.debug(UpdateChecker.class, "Comparing versions...");
                
                int result = version.compare(core.versions().getOrDefault("plugin", "-1"));
                switch(result){
                    case -1 -> printUpdateBanner(version);
                    case 0 -> logger.success("You are running the latest version!");
                    case 1 -> {
                        logger.info(
                            "[?] Your version (<white>%s</white>) is newer than what is available on Modrint (<white>%s</white>).",
                            core.versions().getOrDefault("plugin", "-1"),
                            version.versionNumber()
                        );
                        logger.info("    Are you using a Development Version?");
                    }
                }
            });
        }, 0L, 12L, TimeUnit.HOURS);
    }
    
    private CompletableFuture<ModrinthVersion> performUpdateCheck(){
        String finalUrl = String.format(
            // 'https://api.modrinth.com/v2/project/advancedserverlist/version?loaders=["%s"]' URL encoded.
            "https://api.modrinth.com/v2/project/advancedserverlist/version?loaders=%%5B%%22%s%%22%%5D",
            loader
        );
        
        logger.debug(UpdateChecker.class, "Checking '%s' for updates...", finalUrl);
        
        if(core.versions().getOrDefault("plugin", "UNKNOWN").equals("UNKNOWN")){
            logger.warn("Cannot perform update check. Plugin version is 'UNKNOWN'!");
            return CompletableFuture.completedFuture(null);
        }
        
        HttpRequest request;
        try{
            request = HttpRequest.newBuilder()
                .uri(new URI(finalUrl))
                .timeout(Duration.ofSeconds(5))
                .header("User-Agent", "AdvancedServerList-" + loader + "/" + core.versions().get("plugin"))
                .build();
        }catch(URISyntaxException ex){
            logger.warn("Cannot perform update check. URL '%s' is not a valid URI.", ex, finalUrl);
            return CompletableFuture.completedFuture(null);
        }
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            .thenApply(response -> new ArrayList<ModrinthVersion>(gson.fromJson(response.body(), listType)))
            .thenApply(ArrayList::getFirst)
            .exceptionally((throwable) -> {
                logger.warn("Encountered exception while performing update check!", throwable);
                return null;
            });
    }
    
    private void printUpdateBanner(ModrinthVersion version){
        logger.warn("=======================================================================");
        logger.warn("You are running an outdated version of AdvancedServerList!");
        logger.warn("");
        logger.warn("Your version: <white>%s</white>", core.versions().get("plugin"));
        logger.warn("Modrinth:     <white>%s</white>", version.versionNumber());
        logger.warn("");
        
        if(!version.isRelease()){
            logger.warn("<bold>WARNING!</bold>");
            logger.warn("This is a %s version and may contain breaking changes and/or bugs!", version.versionType());
            logger.warn("");
        }
        
        logger.warn("Download the latest version from here:");
        logger.warn("<white>https://modrinth.com/plugin/advancedserverlist/version/%s</white>", version.id());
        logger.warn("=======================================================================");
    }
    
    public record ModrinthVersion(String id, String versionNumber, String versionType){
        
        public boolean isRelease(){
            return versionType().equals("release");
        }
        
        public int compare(String version){
            return FlexVerComparator.compare(version, versionNumber());
        }
    }
}
