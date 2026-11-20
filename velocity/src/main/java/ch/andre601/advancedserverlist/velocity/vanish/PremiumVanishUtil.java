package ch.andre601.advancedserverlist.velocity.vanish;

import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;

public class PremiumVanishUtil{
    
    public static boolean isVanished(Player player){
        return VelocityVanishAPI.isInvisible(player);
    }
}
