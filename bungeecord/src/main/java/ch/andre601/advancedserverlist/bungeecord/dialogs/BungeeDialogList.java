package ch.andre601.advancedserverlist.bungeecord.dialogs;

import ch.andre601.advancedserverlist.bungeecord.BungeeComponentParser;
import ch.andre601.advancedserverlist.bungeecord.commands.BungeeCmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialogList;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.DialogListDialog;
import net.md_5.bungee.api.dialog.body.DialogBody;
import net.md_5.bungee.api.dialog.body.PlainMessageBody;

import java.util.ArrayList;
import java.util.List;

public class BungeeDialogList implements IDialogList{
    
    private final String title;
    private final List<String> bodies;
    private final List<IDialog<?>> dialogs;
    
    private BungeeDialogList(String title, List<String> bodies, List<IDialog<?>> dialogs){
        this.title = title;
        this.bodies = bodies;
        this.dialogs = dialogs;
    }
    
    @Override
    public void show(CmdSender sender){
        List<DialogBody> dialogBodies = bodies.stream()
            .<DialogBody>map(text -> new PlainMessageBody(BungeeComponentParser.parse(text)))
            .toList();
        List<Dialog> bungeeDialogs = dialogs.stream()
            .filter(dialog -> dialog instanceof BungeeDialog)
            .map(dialog -> (BungeeDialog)dialog)
            .map(BungeeDialog::createDialog)
            .toList();
        
        DialogListDialog dialog = new DialogListDialog(
            new DialogBase(BungeeComponentParser.parse(title)).body(dialogBodies),
            bungeeDialogs,
            null,
            null,
            null
        ).columns(3);
        
        if(!(sender instanceof BungeeCmdSender bungeeSender)){
            sender.sendErrorMsg("<red>Command can only be executed by a Bungee Player!");
            return;
        }
        
        if(!(bungeeSender.sender() instanceof ProxiedPlayer player)){
            sender.sendErrorMsg("<red>Command can only be executed by a Player!");
            return;
        }
        
        player.showDialog(dialog);
    }
    
    public static class BungeeDialogListBuilder implements IDialogListBuilder{
        
        public static BungeeDialogListBuilder create(String title){
            return new BungeeDialogListBuilder(title);
        }
        
        private final String title;
        private final List<String> bodies = new ArrayList<>();
        private final List<IDialog<?>> dialogs = new ArrayList<>();
        
        private BungeeDialogListBuilder(String title){
            this.title = title;
        }
        
        @Override
        public BungeeDialogListBuilder appendBody(String body){
            this.bodies.add(body);
            return this;
        }
        
        @Override
        public BungeeDialogListBuilder appendDialog(IDialog<?> dialog){
            this.dialogs.add(dialog);
            return this;
        }
        
        @Override
        public BungeeDialogList build(){
            return new BungeeDialogList(title, bodies, dialogs);
        }
    }
}
