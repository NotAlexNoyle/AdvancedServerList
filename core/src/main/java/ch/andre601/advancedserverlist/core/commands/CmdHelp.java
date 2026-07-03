package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Command("advancedserverlist|asl")
@CommandDescription("Main Command of the plugin")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public record CmdHelp(CommandManager<CmdSender> manager, MinecraftHelp<CmdSender> help){
    
    public static CmdHelp create(CommandManager<CmdSender> manager){
        MinecraftHelp<CmdSender> help = MinecraftHelp.<CmdSender>builder()
            .commandManager(manager)
            .audienceProvider(CmdSender::audience)
            .commandPrefix("/asl help")
            .colors(MinecraftHelp.helpColors(
                NamedTextColor.WHITE, NamedTextColor.AQUA, NamedTextColor.WHITE, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY
            ))
            .build();
        
        return new CmdHelp(manager, help);
    }
    
    @Command("help [query]")
    @CommandDescription("Displays all commands or provides info about a specific one.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.help"})
    @CommandType(CommandType.Type.ALL)
    public void handle(
        CmdSender sender,
        @Nullable @Argument(description = "Search Query for a specific command", suggestions = "help") @Greedy String query
    ){
        this.help.queryCommands(query == null ? "" : query, sender);
    }
    
    @Suggestions("help")
    public List<String> helpQueries(CommandContext<CmdSender> context) {
        return this.manager.createHelpHandler()
            .queryRootIndex(context.sender())
            .entries()
            .stream()
            .filter(entry -> context.hasPermission(entry.command().commandPermission()))
            .filter(entry -> {
                CommandType.Type type = entry.command().commandMeta().getOrDefault(CommandHandler.COMMAND_TYPE, CommandType.Type.ALL);
                
                return type == CommandType.Type.ALL || type == CommandType.Type.PLAYER_ONLY && context.sender().isPlayer();
            })
            .map(CommandEntry::syntax)
            .toList();
    }
}
