package ch.andre601.advancedserverlist.luckperms.velocity;

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI;
import ch.andre601.advancedserverlist.api.exceptions.InvalidPlaceholderProviderException;
import ch.andre601.advancedserverlist.luckperms.placeholders.LuckPermsPlaceholder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuckPermsAddon{
    
    private final ProxyServer proxy;
    private final Logger logger = LoggerFactory.getLogger("ASLLuckPermsAddon");
    
    @Inject
    public LuckPermsAddon(ProxyServer proxy){
        this.proxy = proxy;
    }
    
    @Subscribe
    public void init(ProxyInitializeEvent event){
        if(!proxy.getPluginManager().isLoaded("advancedserverlist")){
            logger.warn("AdvancedServerList is not enabled. This plugin is required.");
            return;
        }
        
        if(!proxy.getPluginManager().isLoaded("luckperms")){
            logger.warn("LuckPerms is not enabled. This plugin is required.");
            return;
        }
        
        LuckPerms luckPerms;
        try{
            luckPerms = LuckPermsProvider.get();
        }catch(IllegalStateException ex){
            logger.warn("Unable to obtain LuckPerms API instance. The plugin won't work.");
            return;
        }
        
        logger.info("Registering LuckPerms Placeholder...");
        try{
            AdvancedServerListAPI.get().addPlaceholderProvider(new LuckPermsPlaceholder(luckPerms));
            logger.info("Successfully registered Placeholder!");
        }catch(InvalidPlaceholderProviderException ex){
            logger.warn("Unable to register LuckPerms Placeholder! The plugin won't work.");
        }
    }
}
