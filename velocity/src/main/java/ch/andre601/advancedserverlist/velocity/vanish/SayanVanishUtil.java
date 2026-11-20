package ch.andre601.advancedserverlist.velocity.vanish;

import com.velocitypowered.api.proxy.Player;
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI;

public class SayanVanishUtil{
    
    public static boolean isVanished(Player player){
        return SayanVanishVelocityAPI.getInstance().isVanished(player.getUniqueId());
    }
}
