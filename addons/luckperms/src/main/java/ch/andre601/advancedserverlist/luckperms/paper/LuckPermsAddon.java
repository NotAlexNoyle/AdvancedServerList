package ch.andre601.advancedserverlist.luckperms.paper;

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI;
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException;
import ch.andre601.advancedserverlist.luckperms.placeholders.LuckPermsPlaceholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckPermsAddon extends JavaPlugin{
    
    @Override
    public void onEnable(){
        if(!this.getServer().getPluginManager().isPluginEnabled("AdvancedServerList")){
            this.getSLF4JLogger().warn("AdvancedServerList not enabled. This plugin is required!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if(!this.getServer().getPluginManager().isPluginEnabled("LuckPerms")){
            this.getSLF4JLogger().warn("LuckPerms not enabled. This plugin is required!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        LuckPerms luckPerms;
        try{
            luckPerms = LuckPermsProvider.get();
        }catch(IllegalStateException ex){
            this.getSLF4JLogger().error("Unable to obtain LuckPerms API instance! Disabling plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        this.getSLF4JLogger().info("Registering LuckPerms Placeholder...");
        try{
            AdvancedServerListAPI.get().addPlaceholderProvider(new LuckPermsPlaceholder(luckPerms));
            this.getSLF4JLogger().info("Successfully registered Placeholder!");
        }catch(InvalidPlaceholderProviderException ex){
            this.getSLF4JLogger().warn("Couldn't register Placeholder. Disabling Plugin...");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }
}
