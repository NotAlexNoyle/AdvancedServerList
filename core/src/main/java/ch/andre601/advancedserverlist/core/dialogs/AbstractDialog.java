package ch.andre601.advancedserverlist.core.dialogs;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;

import java.util.List;

public abstract class AbstractDialog{
    
    protected final String title;
    protected final ServerListProfile profile;
    protected final int profilesPosition;
    
    protected AbstractDialog(String title, ServerListProfile profile, int profilesPosition){
        this.title = title;
        this.profile = profile;
        this.profilesPosition = profilesPosition;
    }
    
    public abstract <T> T createDialog();
    
    public static abstract class AbstractDialogBuilder{
        
        protected final String title;
        protected final ServerListProfile profile;
        protected final int profilesPosition;
        
        protected AbstractDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
            this.title = title;
            this.profile = profile;
            this.profilesPosition = profilesPosition;
        }
        
        public abstract AbstractDialogBuilder appendBody(String body);
        
        public abstract AbstractDialogBuilder appendTextInput(String key, String title, String initValue, int maxLength);
        
        public abstract AbstractDialogBuilder appendTextbox(String key, String title, List<String> initValue, int maxLength, int rows);
        
        public abstract AbstractDialogBuilder appendNumberRange(String key, String title, int initValue);
        
        public abstract AbstractDialogBuilder appendBoolean(String key, String title, NullBool initValue);
        
        public abstract AbstractDialog build();
    }
}
