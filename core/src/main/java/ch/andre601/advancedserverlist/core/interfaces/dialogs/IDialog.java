package ch.andre601.advancedserverlist.core.interfaces.dialogs;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.core.commands.CmdProfiles;

import java.util.List;

public interface IDialog<T>{
    T createDialog();
    
    interface IDialogBuilder{
        IDialogBuilder appendBody(String body);
        
        IDialogBuilder appendTextInput(CmdProfiles.ProfileOption option, String title, String initValue, int maxLength);
        
        IDialogBuilder appendTextbox(CmdProfiles.ProfileOption option, String title, List<String> initValue, int maxLength, int rows);
        
        IDialogBuilder appendNumberRange(CmdProfiles.ProfileOption option, String title, int initValue);
        
        IDialogBuilder appendBoolean(CmdProfiles.ProfileOption option, String title, NullBool initValue);
        
        IDialog<?> build();
    }
}
