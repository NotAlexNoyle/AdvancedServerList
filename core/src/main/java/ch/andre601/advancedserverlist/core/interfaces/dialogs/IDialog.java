package ch.andre601.advancedserverlist.core.interfaces.dialogs;

import ch.andre601.advancedserverlist.api.objects.NullBool;

import java.util.List;

public interface IDialog<T>{
    T createDialog();
    
    interface IDialogBuilder{
        IDialogBuilder appendBody(String body);
        
        IDialogBuilder appendTextInput(String key, String title, String initValue, int maxLength);
        
        IDialogBuilder appendTextbox(String key, String title, List<String> initValue, int maxLength, int rows);
        
        IDialogBuilder appendNumberRange(String key, String title, int initValue);
        
        IDialogBuilder appendBoolean(String key, String title, NullBool initValue);
        
        IDialog<?> build();
    }
}
