package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.services.type.ConsumerService;

public class CommandTypePostprocessor implements CommandPostprocessor<CmdSender>{
    @Override
    public void accept(@NonNull CommandPostprocessingContext<CmdSender> context){
        CmdSender sender = context.commandContext().sender();
        CommandType.Type type = context.commandContext().command().commandMeta().get(CommandHandler.COMMAND_TYPE);
        
        if(type == CommandType.Type.ALL || (type == CommandType.Type.PLAYER_ONLY && sender.isPlayer()))
            return;
        
        sender.sendErrorMsg("<red>This command can only be executed by players!");
        ConsumerService.interrupt();
    }
}
