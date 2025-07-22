package ch.andre601.advancedserverlist.core.interfaces.dialogs;

import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;

public interface IDialogList{
    
    void show(CmdSender sender);
    
    interface IDialogListBuilder{
        IDialogListBuilder appendBody(String body);
        
        IDialogListBuilder appendDialog(IDialog<?> dialog);
        
        IDialogList build();
    }
}
