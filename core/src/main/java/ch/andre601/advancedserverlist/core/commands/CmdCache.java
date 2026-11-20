package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandInput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Command("advancedserverlist|asl")
@CommandDescription("Main Command of the plugin")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public record CmdCache(AdvancedServerList<?> core){
    
    public static CmdCache create(AdvancedServerList<?> core){
        return new CmdCache(core);
    }
    
    @Command("cache clear [target]")
    @CommandDescription("Clears the Favicon and/or Player Cache.")
    @Permission({
        "advancedserverlist.admin",
        "advancedserverlist.command.cache",
        "advancedserverlist.command.cache.clear",
        "advancedserverlist.command.clearcache" // TODO: deprecated. Remove in future update.
    })
    @CommandType(CommandType.Type.ALL)
    public void handle(
        CmdSender sender,
        @Nullable @Argument(description = "The type of Cache to clean. Defaults to ALL.") CacheTarget target
    ){
        if(target == null)
            target = CacheTarget.ALL;
        
        sender.sendPrefixedMsg("Clearing cache...");
        
        if(target == CacheTarget.ALL || target == CacheTarget.FAVICON){
            core().clearFaviconCache();
            sender.sendPrefixedMsg("Cleared Favicon Cache.");
        }
        
        if(target == CacheTarget.ALL || target == CacheTarget.PLAYER){
            core().clearPlayerCache();
            sender.sendPrefixedMsg("Cleared Player Cache.");
        }
        
        sender.sendPrefixedMsg("<green>Cache clearing complete!");
    }
    
    @Command("cache remove <uuid>")
    @CommandDescription("Removes a player from the cache based on the provided UUID.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.cache", "advancedserverlist.command.cache.remove"})
    @CommandType(CommandType.Type.ALL)
    public void remove(
        CmdSender sender,
        @NonNull @Argument(description = "UUID of the player to remove from Storage", suggestions = "cached_players") UUID uuid
    ){
        
        CachedPlayer target = core().playerHandler().player(uuid);
        if(target == null){
            sender.sendErrorMsg("No Cached player with UUID <white>%s</white> found!", uuid.toString());
            return;
        }
        
        if(core().playerHandler().remove(target)){
            sender.sendPrefixedMsg("<green>Successfully removed Cached player <white>%s</white>!", target.name());
        }else{
            sender.sendErrorMsg("Unable to remove player <white>%s</white> from cache. Where they even cached?", target.name());
        }
    }
    
    @Suggestions("cached_players")
    public List<String> playerSuggestions(CommandInput input){
        String value = input.readString();
        List<String> uuids = core().playerHandler().players().stream()
            .map(player -> player.uuid().toString())
            .toList();
        
        if(value.isEmpty())
            return uuids;
        
        List<String> filtered = new ArrayList<>(uuids.size());
        for(String uuid : uuids){
            if(uuid.length() >= value.length() && uuid.regionMatches(true, 0, value, 0, value.length()))
                filtered.add(uuid);
        }
        
        return filtered;
    }
    
    public enum CacheTarget{
        ALL,
        FAVICON,
        PLAYER
    }
}
