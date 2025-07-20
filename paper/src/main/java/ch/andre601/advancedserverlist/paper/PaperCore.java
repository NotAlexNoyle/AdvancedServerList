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

package ch.andre601.advancedserverlist.paper;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.dialogs.AbstractDialog;
import ch.andre601.advancedserverlist.core.dialogs.AbstractDialogList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.handlers.FaviconHandler;
import ch.andre601.advancedserverlist.paper.commands.PaperCommandManagerInterface;
import ch.andre601.advancedserverlist.paper.dialogs.PaperDialog;
import ch.andre601.advancedserverlist.paper.dialogs.PaperDialogList;
import ch.andre601.advancedserverlist.paper.listeners.ServerLoadEventListener;
import ch.andre601.advancedserverlist.paper.logging.PaperLogger;
import ch.andre601.advancedserverlist.paper.objects.WorldCache;
import ch.andre601.advancedserverlist.paper.objects.placeholders.PAPIPlaceholders;
import ch.andre601.advancedserverlist.paper.objects.placeholders.PaperPlayerPlaceholders;
import ch.andre601.advancedserverlist.paper.objects.placeholders.PaperServerPlaceholders;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.PaperLibraryManager;
import io.papermc.paper.ServerBuildInfo;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;
import org.incendo.cloud.CommandManager;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class PaperCore extends JavaPlugin implements PluginCore<CachedServerIcon>{
    
    private final PluginLogger logger = new PaperLogger(this);
    
    private PaperCommandManagerInterface commandHandler;
    private AdvancedServerList<CachedServerIcon> core;
    private FaviconHandler<CachedServerIcon> faviconHandler = null;
    private PAPIPlaceholders papiPlaceholders = null;
    private WorldCache worldCache = null;
    
    private PaperLibraryManager libraryManager = null;
    
    @Override
    public void onEnable(){
    	try{
            // Required for the Cloud command Framework to function.
    		Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
    	}catch(ClassNotFoundException ex){
    		printOutdatedPaper();
    		return;
    	}
        
        this.commandHandler = new PaperCommandManagerInterface(this);
        this.core = AdvancedServerList.init(this, new PaperPlayerPlaceholders(), new PaperServerPlaceholders(this));
        
        if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            papiPlaceholders = new PAPIPlaceholders(this);
    }
    
    @Override
    public void onDisable(){
        if(papiPlaceholders != null){
            papiPlaceholders.unregister();
            papiPlaceholders = null;
        }
        
        if(worldCache != null)
            worldCache = null;
        
        core().disable();
    }
    
    @Override
    public void loadEvents(){
        new ServerLoadEventListener(this);
    }
    
    @Override
    public void loadMetrics(){
        new Metrics(this, 15584).addCustomChart(new SimplePie("profiles",
            () -> String.valueOf(core.fileHandler().profiles().size())
        ));
    }
    
    @Override
    public void loadFaviconHandler(AdvancedServerList<CachedServerIcon> core){
        faviconHandler = new FaviconHandler<>(core);
    }
    
    @Override
    public void clearFaviconCache(){
        if(faviconHandler == null)
            return;
        
        faviconHandler.cleanCache();
    }
    
    @Override
    public void downloadLibrary(String groupId, String artifactId, String version){
        if(libraryManager == null){
            libraryManager = new PaperLibraryManager(this);
            libraryManager.addRepository("https://repo.papermc.io/repository/maven-public");
        }
        
        Library lib = Library.builder()
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .resolveTransitiveDependencies(true)
            .build();
        
        libraryManager.loadLibrary(lib);
    }
    
    @Override
    public void startScheduler(){
        // Unused
    }
    
    @Override
    public AdvancedServerList<CachedServerIcon> core(){
        return core;
    }
    
    @Override
    public Path folderPath(){
        return getDataFolder().toPath();
    }
    
    @Override
    public PluginLogger pluginLogger(){
        return logger;
    }
    
    @Override
    public FaviconHandler<CachedServerIcon> faviconHandler(){
        if(faviconHandler == null)
            faviconHandler = new FaviconHandler<>(core);
        
        return faviconHandler;
    }
    
    @Override
    public CommandManager<CmdSender> commandManager(){
        return commandHandler.commandHandler();
    }
    
    @Override
    public String platformInfo(){
        try{
            Class.forName("io.papermc.paper.ServerBuildInfo");
            return buildInfo();
        }catch(ClassNotFoundException ignored){
            // Old Paper versions (Before 16th of May 2024)
            return version();
        }
    }
    
    @Override
    public String loader(){
        return "paper";
    }
    
    @Override
    public boolean isPluginEnabled(String plugin){
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }
    
    @Override
    public boolean isDialogsSupported(){
        try{
            Class.forName("io.papermc.paper.dialog.Dialog");
            return true;
        }catch(ClassNotFoundException ignored){
            return false;
        }
    }
    
    @Override
    public CachedServerIcon createFavicon(BufferedImage image) throws Exception{
        return Bukkit.loadServerIcon(image);
    }
    
    @Override
    public AbstractDialogList.AbstractDialogListBuilder createDialogListBuilder(String title){
        return PaperDialogList.PaperDialogListBuilder.create(title);
    }
    
    @Override
    public AbstractDialog.AbstractDialogBuilder createDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
        return PaperDialog.PaperDialogBuilder.create(title, profile, profilesPosition);
    }
    
    public WorldCache worldCache(){
        if(worldCache != null)
            return worldCache;
        
        return (worldCache = new WorldCache());
    }
    
    public int playersOnline(World world){
        List<? extends Player> players = new ArrayList<>(world == null ? getServer().getOnlinePlayers() : world.getPlayers());
        
        players.removeIf(player -> {
            for(MetadataValue metadata : player.getMetadata("vanished")){
                if(metadata.asBoolean())
                    return true;
            }
            
            return false;
        });
        
        return players.size();
    }
    
    private String buildInfo(){
        ServerBuildInfo info = ServerBuildInfo.buildInfo();
        
        String name = info.brandName();
        String id = info.brandId().asString();
        String version = info.minecraftVersionName();
        
        OptionalInt build = info.buildNumber();
        if(build.isEmpty())
            return String.format("%s (ID: %s, Version: %s)", name, id, version);
        
        return String.format("%s (ID: %s, Version: %s, Build: %d)", name, id, version, build.getAsInt());
    }
    
    private String version(){
        return getServer().getName() + " " + getServer().getVersion();
    }
    
    private void printOutdatedPaper(){
    	pluginLogger().warn("================================================================");
    	pluginLogger().warn("OUTDATED PAPER VERSION DETECTED!");
    	pluginLogger().warn("");
    	pluginLogger().warn("AdvancedServerList detected an outdated Paper version.");
    	pluginLogger().warn("The plugin requires at least 1.20.6 of Paper due to required");
    	pluginLogger().warn("classes only existing within this version onwards.");
    	pluginLogger().warn("");
    	pluginLogger().warn("Please use at least Paper 1.20.6 or downgrade AdvancedServerList");
    	pluginLogger().warn("to v5.4.1.");
    	pluginLogger().warn("================================================================");
    }
}
