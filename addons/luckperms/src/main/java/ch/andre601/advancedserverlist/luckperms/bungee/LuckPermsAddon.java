package ch.andre601.advancedserverlist.luckperms.bungee;

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI;
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException;
import ch.andre601.advancedserverlist.luckperms.placeholders.LuckPermsPlaceholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.plugin.Plugin;

public class LuckPermsAddon extends Plugin{
    
    @Override
    public void onEnable(){
        if(getProxy().getPluginManager().getPlugin("AdvancedServerList") == null){
            getLogger().warning("AdvancedServerList not found. This plugin is required.");
            return;
        }
        if(getProxy().getPluginManager().getPlugin("LuckPerms") == null){
            getLogger().warning("LuckPerms not found. This plugin is required.");
            return;
        }
        
        LuckPerms luckPerms;
        try{
            luckPerms = LuckPermsProvider.get();
        }catch(IllegalStateException ex){
            getLogger().severe("Unable to obtain LuckPerms API instance! The Plugin won't work.");
            return;
        }
        
        getLogger().info("Registering LuckPerms Placeholders...");
        try{
            AdvancedServerListAPI.get().addPlaceholderProvider(new LuckPermsPlaceholder(luckPerms));
            getLogger().info("Successfully registered Placeholder!");
        }catch(InvalidPlaceholderProviderException ex){
            getLogger().warning("Couldn't register Placeholder. The plugin won't work.");
        }
    }
}
