package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;


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
        
        if(core().fileHandler().reloadConfig()){
            sender.sendPrefixedMsg("<green>Reloaded </green>config.yml<green>!");
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
