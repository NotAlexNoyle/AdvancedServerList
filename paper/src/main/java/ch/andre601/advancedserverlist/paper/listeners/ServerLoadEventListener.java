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

package ch.andre601.advancedserverlist.paper.listeners;

import ch.andre601.advancedserverlist.paper.PaperCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerLoadEventListener implements Listener{
    
    private final PaperCore plugin;
    
    public ServerLoadEventListener(PaperCore plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onLoad(ServerLoadEvent event){
        new PlayerJoinEventListener(plugin);
        new ServerListPingEventListener(plugin);
        new WorldEventsListener(plugin);
        
        // Load currently loaded worlds into the WorldCache
        plugin.worldCache().clear();
        Bukkit.getWorlds().forEach(world -> plugin.worldCache().addWorld(world));
        
        // Print a warning if server load was after a reload.
        if(event.getType() != ServerLoadEvent.LoadType.RELOAD)
            return;
        
        plugin.pluginLogger().warn("======================================================================================");
        plugin.pluginLogger().warn("SERVER RELOAD DETECTED!");
        plugin.pluginLogger().warn("");
        plugin.pluginLogger().warn("A server reload has been detected by AdvancedServerList, meaning either the /reload");
        plugin.pluginLogger().warn("command or Bukkit.reload() was executed.");
        plugin.pluginLogger().warn("");
        plugin.pluginLogger().warn("YOU WILL GET NO SUPPORT FOR THE PLUGIN FOR ANY ISSUES YOU ENCOUNTER AFTER A SERVER");
        plugin.pluginLogger().warn("RELOAD!");
        plugin.pluginLogger().warn("ALWAYS RESTART YOUR SERVER. NEVER RELOAD IT!");
        plugin.pluginLogger().warn("======================================================================================");
    }
}
