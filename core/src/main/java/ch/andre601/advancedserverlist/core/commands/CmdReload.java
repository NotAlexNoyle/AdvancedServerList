package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.Handler;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.Locale;


@Command("advancedserverlist|asl")
@CommandDescription("Main Command of the plugin")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public record CmdReload(AdvancedServerList<?> core){
    
    public static CmdReload create(AdvancedServerList<?> core){
        return new CmdReload(core);
    }
    
    @Command("reload")
    @CommandDescription("Reloads the plugin's main config and its profiles.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.reload"})
    @CommandType(CommandType.Type.ALL)
    public void handle(CmdSender sender){
        sender.sendPrefixedMsg("Reloading plugin...");
        
        Handler handler = Handler.fromString(core().fileHandler().getString("", "storage", "type"));
        
        if(core().fileHandler().reloadConfig()){
            sender.sendPrefixedMsg("<green>Reloaded </green>config.yml<green>!");
            
            Handler newHandler = Handler.fromString(core().fileHandler().getString("", "storage", "type"));
            if(newHandler == Handler.UNKNOWN){
                sender.sendErrorMsg(
                    "Received invalid Storage type <white>%s</white>. Plugin will default to JSON on restart if not fixed.",
                    core().fileHandler().getString("", "storage", "type")
                );
            }else
            if(newHandler != handler){
                sender.sendPrefixedMsg(
                    "Detected Storage type change (<white>%s -> %s</white>). These changes will only take effect after a restart.",
                    handler.name().toLowerCase(Locale.ROOT),
                    newHandler.name().toLowerCase(Locale.ROOT)
                );
            }
        }else{
            sender.sendErrorMsg("<red>Encountered an error while reloading the </red>config.yml<red>.");
            sender.sendErrorMsg("<red>See the server console for details.");
        }
        
        if(core().fileHandler().reloadProfiles()){
            sender.sendPrefixedMsg("<green>(Re)loaded <white>%d</white> Profile(s)!", core().fileHandler().profiles().size());
        }else{
            sender.sendErrorMsg("<red>Encountered errors while (re)loading profiles.");
            sender.sendErrorMsg("<red>See the server console for details.");
        }
        
        sender.sendPrefixedMsg("<green>Reload complete!");
    }
}
