package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.jspecify.annotations.Nullable;


@Command("advancedserverlist|asl")
@CommandDescription("Main Command of the plugin")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public record CmdClearCache(AdvancedServerList<?> core){
    
    public static CmdClearCache create(AdvancedServerList<?> core){
        return new CmdClearCache(core);
    }
    
    @Command("clearcache [target]")
    @CommandDescription("Cleares the Favicon and/or Player Cache.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.clearcache"})
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
    
    public enum CacheTarget{
        ALL,
        FAVICON,
        PLAYER
    }
}
