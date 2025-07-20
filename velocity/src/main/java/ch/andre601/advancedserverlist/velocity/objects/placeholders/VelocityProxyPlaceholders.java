package ch.andre601.advancedserverlist.velocity.objects.placeholders;

import ch.andre601.advancedserverlist.api.PlaceholderProvider;
import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.velocity.VelocityCore;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class VelocityProxyPlaceholders extends PlaceholderProvider{
    
    private final VelocityCore plugin;
    
    private VelocityProxyPlaceholders(VelocityCore plugin){
        super("proxy");
        
        this.plugin = plugin;
    }
    
    public static VelocityProxyPlaceholders init(VelocityCore plugin){
        return new VelocityProxyPlaceholders(plugin);
    }
    
    @Override
    public @Nullable String parsePlaceholder(String placeholder, GenericPlayer player, GenericServer server){
        String[] args = placeholder.split("\\s", 2);
        if(args.length < 2)
            return null;
        
        ServerPing ping = plugin.getFetchedServers().get(args[1]);
        return switch(args[0]){
            case "status" -> ping == null ? "offline" : "online";
            case "motd" -> {
                if(ping == null)
                    yield "none";
                
                yield ComponentParser.toMMString(ping.getDescriptionComponent());
            }
            case "players" -> {
                if(ping == null)
                    yield "0";
                
                ServerPing.Players players = ping.getPlayers().orElse(null);
                if(players == null)
                    yield "0";
                
                yield String.valueOf(players.getOnline());
            }
            case "maxPlayers" -> {
                if(ping == null)
                    yield "0";
                
                ServerPing.Players players = ping.getPlayers().orElse(null);
                if(players == null)
                    yield "0";
                
                yield String.valueOf(players.getMax());
            }
            default -> null;
        };
    }
}
