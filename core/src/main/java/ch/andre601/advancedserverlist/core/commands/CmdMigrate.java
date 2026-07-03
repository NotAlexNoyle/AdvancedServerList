package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import ch.andre601.advancedserverlist.core.migration.minimotd.MiniMOTDConfigMigrator;
import ch.andre601.advancedserverlist.core.migration.serverlistplus.SLPConfigMigrator;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

@Command("advancedserverlist|asl")
@CommandDescription("Main Command of the plugin")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public record CmdMigrate(AdvancedServerList<?> core){
    
    public static CmdMigrate create(AdvancedServerList<?> core){
        return new CmdMigrate(core);
    }
    
    @Command("migrate <plugin>")
    @CommandDescription("Migrates Profiles from supported Plugins to ASL.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.migrate"})
    @CommandType(CommandType.Type.ALL)
    public void handle(
        CmdSender sender,
        @NonNull @Argument(description = "Name of the plugin to migrate from") MigratablePlugin plugin
    ){
        if(plugin == MigratablePlugin.SERVERLISTPLUS){
            if(!core().plugin().isPluginEnabled("ServerListPlus")){
                sender.sendErrorMsg("<red>ServerListPlus not enabled.");
                sender.sendErrorMsg("<red>The plugin needs to be enabled for the migration to work.");
                return;
            }
            
            sender.sendPrefixedMsg("Migrating from ServerListPlus...");
            
            int migrated = SLPConfigMigrator.migrate(core(), sender);
            if(migrated == 0){
                sender.sendErrorMsg("<red>Couldn't migrate any profiles from ServerListPlus.");
                sender.sendErrorMsg("<red>See the server console for any errors.");
                return;
            }else{
                sender.sendPrefixedMsg("<green>Successfully migrated </green>%d<green> Profile(s)!", migrated);
            }
        }else
        if(plugin == MigratablePlugin.MINIMOTD){
            sender.sendPrefixedMsg("Migrating from MiniMOTD...");
            
            if(MiniMOTDConfigMigrator.migrate(core(), sender)){
                sender.sendPrefixedMsg("<green>Successfully migrated config from MiniMOTD!");
            }else{
                sender.sendErrorMsg("<red>Couldn't migrate config from MiniMOTD.");
                sender.sendErrorMsg("<red>See the server console for any errors.");
                return;
            }
        }else{
            sender.sendErrorMsg("<red>Received invalid Plugin </red>%s<red>!", plugin.toString());
            sender.sendErrorMsg("<red>Only the following plugins are supported:");
            
            for(MigratablePlugin pl : MigratablePlugin.values()){
                sender.sendErrorMsg("- <click:suggest_command:/asl migrate %s>%s</click>", pl.name().toLowerCase(Locale.ROOT), pl.name().toLowerCase(Locale.ROOT));
            }
        }
        
        sender.sendPrefixedMsg("<green>Run <white><click:suggest_command:/asl reload>/asl reload</click></white> to load the new Profile(s).");
    }
    
    public enum MigratablePlugin{
        SERVERLISTPLUS,
        MINIMOTD
    }
}
