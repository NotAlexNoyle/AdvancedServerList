package ch.andre601.advancedserverlist.bungeecord.listeners;

import ch.andre601.advancedserverlist.bungeecord.BungeeCordCore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.CustomClickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomClickListener implements Listener{
    
    private final BungeeCordCore plugin;
    
    public CustomClickListener(BungeeCordCore plugin){
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCustomClick(CustomClickEvent event){
        String id = event.getId();
        if(!id.toLowerCase(Locale.ROOT).startsWith("advancedserverlist:save/"))
            return;
        
        String[] split = id.substring(0, "advancedserverlist:save/".length()).split("/");
        if(split.length < 2)
            return;
        
        ProxiedPlayer player = event.getPlayer();
        JsonElement data = event.getData();
        if(data == null || !data.isJsonObject())
            return;
        
        JsonObject json = data.getAsJsonObject();
        for(Map.Entry<String, JsonElement> entry : json.entrySet()){
            plugin.pluginLogger().warn("JSONEntry{key=%s,value=%s}", entry.getKey(), entry.getValue().getAsString());
        }
    }
}
