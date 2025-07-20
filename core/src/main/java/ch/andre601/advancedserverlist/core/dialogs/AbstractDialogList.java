package ch.andre601.advancedserverlist.core.dialogs;

import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;

import java.util.List;

public abstract class AbstractDialogList{
    
    protected String title;
    protected List<String> bodies;
    protected List<AbstractDialog> dialogs;
    
    protected AbstractDialogList(String title, List<String> bodies, List<AbstractDialog> dialogs){
        this.title = title;
        this.bodies = bodies;
        this.dialogs = dialogs;
    }
    
    public abstract void show(CmdSender sender);
    
    public static abstract class AbstractDialogListBuilder{
        protected final String title;
        
        protected AbstractDialogListBuilder(String title){
            this.title = title;
        }
        
        public abstract AbstractDialogListBuilder appendBody(String body);
        
        public abstract AbstractDialogListBuilder appendDialog(AbstractDialog dialog);
        
        public abstract AbstractDialogList build();
    }
}
