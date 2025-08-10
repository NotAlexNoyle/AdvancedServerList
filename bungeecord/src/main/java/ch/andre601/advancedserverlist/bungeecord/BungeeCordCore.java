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

package ch.andre601.advancedserverlist.bungeecord;

import ch.andre601.advancedserverlist.bungeecord.commands.BungeeCommandManagerInterface;
import ch.andre601.advancedserverlist.bungeecord.dialogs.BungeeDialog;
import ch.andre601.advancedserverlist.bungeecord.dialogs.BungeeDialogList;
import ch.andre601.advancedserverlist.bungeecord.listeners.CustomClickListener;
import ch.andre601.advancedserverlist.bungeecord.listeners.PlayerJoinEventListener;
import ch.andre601.advancedserverlist.bungeecord.listeners.ProxyPingEventListener;
import ch.andre601.advancedserverlist.bungeecord.logging.BungeeLogger;
import ch.andre601.advancedserverlist.bungeecord.objects.placeholders.BungeePlayerPlaceholders;
import ch.andre601.advancedserverlist.bungeecord.objects.placeholders.BungeeProxyPlaceholders;
import ch.andre601.advancedserverlist.bungeecord.objects.placeholders.BungeeServerPlaceholders;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialogList;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.handlers.FaviconHandler;
import com.alessiodp.libby.BungeeLibraryManager;
import com.alessiodp.libby.Library;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.incendo.cloud.CommandManager;
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeCordCore extends Plugin implements PluginCore<Favicon>{
    
    private final PluginLogger logger = new BungeeLogger(this);
    private final Map<String, ServerPing> fetchedServers = new ConcurrentHashMap<>();
    
    private boolean successfulLoad = false;
    
    private AdvancedServerList<Favicon> core;
    private FaviconHandler<Favicon> faviconHandler = null;
    private BungeeAudiences audiences = null;
    private BungeeCommandManagerInterface handler;
    
    private BungeeLibraryManager libraryManager = null;
    
    @Override
    public void onLoad(){
        // Using getLogger() here, because PluginLogger requires MiniMessage, which isn't loaded at this stage.
        getLogger().info("Loading libraries. This may take a while...");
        successfulLoad = loadLibs();
        if(successfulLoad){
            getLogger().info("Library loading completed successfully!");
        }else{
            getLogger().warning("Library loading completed with errors!");
        }
    }
    
    @Override
    public void onEnable(){
        if(!successfulLoad){
            logger.warn("There were issues during the plugins Loading-phase! Please see the console for possible reasons.");
            logger.warn("Aborting further enabling of the plugin to avoid errors.");
            return;
        }
        
        this.audiences = BungeeAudiences.create(this);
        this.handler = new BungeeCommandManagerInterface(this);
        this.core = AdvancedServerList.init(this,
            BungeePlayerPlaceholders.init(), BungeeServerPlaceholders.init(this), BungeeProxyPlaceholders.init(this));
    }
    
    @Override
    public void onDisable(){
        core.disable();
        getProxy().getScheduler().cancel(this);
        audiences.close();
    }
    
    @Override
    public void loadEvents(){
        new PlayerJoinEventListener(this);
        new ProxyPingEventListener(this);
        
        if(isDialogsSupported())
            new CustomClickListener(this);
    }
    
    @Override
    public void loadMetrics(){
        new Metrics(this, 15585).addCustomChart(new SimplePie("profiles",
            () -> String.valueOf(core.fileHandler().profiles().size())
        ));
        
    }
    
    @Override
    public void loadFaviconHandler(AdvancedServerList<Favicon> core){
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
            libraryManager = new BungeeLibraryManager(this);
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
        getProxy().getScheduler().schedule(this, this::fetchServers, 0, 10, TimeUnit.SECONDS);
    }
    
    @Override
    public AdvancedServerList<Favicon> core(){
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
    public FaviconHandler<Favicon> faviconHandler(){
        if(faviconHandler == null)
            faviconHandler = new FaviconHandler<>(core);
        
        return faviconHandler;
    }
    
    @Override
    public CommandManager<CmdSender> commandManager(){
        return handler.commandHandler();
    }
    
    @Override
    public String platformInfo(){
        return getProxy().getName() + " " + getProxy().getVersion();
    }
    
    @Override
    public String loader(){
        return "bungeecord";
    }
    
    @Override
    public boolean isPluginEnabled(String plugin){
        return getProxy().getPluginManager().getPlugin(plugin) != null;
    }
    
    @Override
    public boolean isDialogsSupported(){
        try{
            Class.forName("net.md_5.bungee.api.dialog.Dialog");
            return true;
        }catch(ClassNotFoundException ignored){
            return false;
        }
    }
    
    @Override
    public Favicon createFavicon(BufferedImage image) throws IllegalArgumentException{
        return Favicon.create(image);
    }
    
    @Override
    public IDialogList.IDialogListBuilder createDialogListBuilder(String title){
        return BungeeDialogList.BungeeDialogListBuilder.create(title);
    }
    
    @Override
    public IDialog.IDialogBuilder createDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
        return BungeeDialog.BungeeDialogBuilder.create(title, profile, profilesPosition);
    }
    
    public BungeeAudiences audiences(){
        return audiences;
    }
    
    public int onlinePlayers(ServerInfo server){
        List<ProxiedPlayer> players = new ArrayList<>(server == null ? getProxy().getPlayers() : server.getPlayers());
        
        // Exclude players when PremiumVanish is enabled and player is hidden.
        if(getProxy().getPluginManager().getPlugin("PremiumVanish") != null){
            players.removeIf(BungeeVanishAPI::isInvisible);
        }else // Do the same if SayanVanish is enabled.
        if(getProxy().getPluginManager().getPlugin("SayanVanish") != null){
            players.removeIf(player -> SayanVanishBungeeAPI.getInstance().isVanished(player.getUniqueId()));
        }
        
        return players.size();
    }
    
    public Map<String, ServerPing> fetchedServers(){
        return fetchedServers;
    }
    
    private boolean loadLibs(){
        try{
            if(libraryManager == null){
                libraryManager = new BungeeLibraryManager(this);
                libraryManager.addRepository("https://repo.papermc.io/repository/maven-public");
            }
            
            libraryManager.configureFromJSON("dependencies.json");
            
            return true;
        }catch(Exception ex){
            getLogger().log(Level.WARNING, "Encountered an Exception while trying to load dependencies.", ex);
            return false;
        }
    }
    
    private void fetchServers(){
        fetchedServers.clear();
        getProxy().getServers().forEach((name, server) -> server.ping((ping, throwable) -> {
            if(throwable != null)
                return;
            
            fetchedServers.put(name, ping);
        }));
    }
}
