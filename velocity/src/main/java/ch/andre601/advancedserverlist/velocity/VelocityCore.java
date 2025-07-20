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

package ch.andre601.advancedserverlist.velocity;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.dialogs.AbstractDialog;
import ch.andre601.advancedserverlist.core.dialogs.AbstractDialogList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.handlers.FaviconHandler;
import ch.andre601.advancedserverlist.velocity.commands.VelocityCommandManagerInterface;
import ch.andre601.advancedserverlist.velocity.listeners.PlayerJoinEventListener;
import ch.andre601.advancedserverlist.velocity.listeners.ProxyPingEventListener;
import ch.andre601.advancedserverlist.velocity.logging.VelocityLogger;
import ch.andre601.advancedserverlist.velocity.objects.placeholders.VelocityPlayerPlaceholders;
import ch.andre601.advancedserverlist.velocity.objects.placeholders.VelocityProxyPlaceholders;
import ch.andre601.advancedserverlist.velocity.objects.placeholders.VelocityServerPlaceholders;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.VelocityLibraryManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.util.Favicon;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.incendo.cloud.CommandManager;
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class VelocityCore implements PluginCore<Favicon>{
    
    private final Map<String, ServerPing> fetchedServers = new ConcurrentHashMap<>();
    
    private final PluginLogger pluginLogger;
    private final ComponentLogger logger;
    
    private final ProxyServer proxy;
    private final Path path;
    private final Metrics.Factory metrics;
    private final VelocityCommandManagerInterface commandHandler;
    
    private AdvancedServerList<Favicon> core;
    private FaviconHandler<Favicon> faviconHandler = null;
    
    private VelocityLibraryManager<VelocityCore> libraryManager = null;
    
    private ScheduledTask scheduledTask = null;
    
    @Inject
    public VelocityCore(PluginContainer pluginContainer, ProxyServer proxy, @DataDirectory Path path, Metrics.Factory metrics){
        ComponentLogger logger = ComponentLogger.logger("AdvancedServerList");
        this.pluginLogger = new VelocityLogger(this, logger);
        this.logger = logger;
        
        this.proxy = proxy;
        this.path = path;
        this.metrics = metrics;
        this.commandHandler = new VelocityCommandManagerInterface(pluginContainer, proxy);
    }
    
    @Subscribe
    public void init(ProxyInitializeEvent event){
        this.core = AdvancedServerList.init(this,
            VelocityPlayerPlaceholders.init(), VelocityServerPlaceholders.init(this), VelocityProxyPlaceholders.init(this));
    }
    
    @Subscribe
    public void pluginDisable(ProxyShutdownEvent event){
        core.disable();
        if(scheduledTask != null)
            scheduledTask.cancel();
    }
    
    @Override
    public void loadEvents(){
        new PlayerJoinEventListener(this);
        new ProxyPingEventListener(this);
    }
    
    @Override
    public void loadMetrics(){
        metrics.make(this, 15587).addCustomChart(new SimplePie("profiles",
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
            libraryManager = new VelocityLibraryManager<>(this, this.logger, folderPath(), proxy().getPluginManager());
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
        scheduledTask = proxy().getScheduler().buildTask(this, this::fetchServers)
            .repeat(10, TimeUnit.SECONDS)
            .schedule();
    }
    
    @Override
    public AdvancedServerList<Favicon> core(){
        return core;
    }
    
    @Override
    public Path folderPath(){
        return path;
    }
    
    @Override
    public PluginLogger pluginLogger(){
        return pluginLogger;
    }
    
    @Override
    public FaviconHandler<Favicon> faviconHandler(){
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
        return proxy().getVersion().getName() + " " + proxy().getVersion().getVersion();
    }
    
    @Override
    public String loader(){
        return "velocity";
    }
    
    @Override
    public boolean isPluginEnabled(String plugin){
        if(proxy() == null)
            return false;
        
        return proxy().getPluginManager().isLoaded(plugin.toLowerCase(Locale.ROOT));
    }
    
    @Override
    public boolean isDialogsSupported(){
        return false; // Velocity lacks a Dialog System
    }
    
    @Override
    public Favicon createFavicon(BufferedImage image){
        return Favicon.create(image);
    }
    
    @Override
    public AbstractDialogList.AbstractDialogListBuilder createDialogListBuilder(String title){
        return null;
    }
    
    @Override
    public AbstractDialog.AbstractDialogBuilder createDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
        return null;
    }
    
    public ProxyServer proxy(){
        return proxy;
    }
    
    public int onlinePlayers(RegisteredServer server){
        if(proxy() == null)
            return -1;
        
        List<Player> players = new ArrayList<>(server == null ? proxy().getAllPlayers() : server.getPlayersConnected());
        
        // Exclude players when PremiumVanish is enabled and player is hidden.
        if(proxy().getPluginManager().isLoaded("premiumvanish")){
            players.removeIf(VelocityVanishAPI::isInvisible);
        }else // Do the same if SayanVanish is enabled.
        if(proxy().getPluginManager().isLoaded("sayanvanish")){
            players.removeIf(player -> SayanVanishVelocityAPI.getInstance().isVanished(player.getUniqueId()));
        }
        
        return players.size();
    }
    
    public Map<String, ServerPing> getFetchedServers(){
        return fetchedServers;
    }
    
    private void fetchServers(){
        fetchedServers.clear();
        proxy().getAllServers().forEach(server -> server.ping().whenComplete((ping, throwable) -> {
            if(throwable != null)
                return;
            
            fetchedServers.put(server.getServerInfo().getName(), ping);
        }));
    }
}
