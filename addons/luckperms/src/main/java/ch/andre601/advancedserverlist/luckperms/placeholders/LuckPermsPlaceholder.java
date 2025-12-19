package ch.andre601.advancedserverlist.luckperms.placeholders;

import ch.andre601.advancedserverlist.api.PlaceholderProvider;
import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class LuckPermsPlaceholder extends PlaceholderProvider{
    
    private final LuckPerms luckPerms;
    
    private final HashMap<UUID, CacheValue> cache = new HashMap<>();
    
    public LuckPermsPlaceholder(LuckPerms luckPerms){
        super("luckperms");
        this.luckPerms = luckPerms;
    }
    
    @Override
    public @Nullable String parsePlaceholder(@NonNull String placholder, @NonNull GenericPlayer player, @NonNull GenericServer server){
        if(luckPerms == null)
            return "ERR: LuckPerms not available.";
        
        CacheValue cacheValue = cache.get(player.getUUID());
        User user;
        if(cacheValue == null || cacheValue.expired()){
            // Can cause lag if on main thread.
            // But the alternative would be a lot more annoying and error-prone.
            user = luckPerms.getUserManager().loadUser(player.getUUID()).join();
            cache.put(player.getUUID(), new CacheValue(System.currentTimeMillis(), user));
        }else{
            user = cacheValue.user();
        }
        
        if(user == null)
            return "ERR: Player not found.";
        
        String[] parts = placholder.split("\\s+");
        if(parts.length == 1)
            return "ERR: Missing argument for placeholder " + parts[0] + ".";
        
        return switch(parts[0]){
            case "hasGroup" -> String.valueOf(
                user.getCachedData().getPermissionData().checkPermission("group." + parts[1])
            );
            case "hasPermission" -> String.valueOf(
                user.getCachedData().getPermissionData().checkPermission(parts[1]).asBoolean()
            );
            case "primaryGroup" -> user.getPrimaryGroup();
            default -> null;
        };
    }
    
    private record CacheValue(long timestamp, User user){
        public boolean expired(){
            // Expired if current timestamp minus old one is more than 60k milliseconds (1 minute)
            return (timestamp < 0L) || System.currentTimeMillis() - timestamp >= 60_000;
        }
    }
}
