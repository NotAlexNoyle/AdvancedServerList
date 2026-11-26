package ch.andre601.advancedserverlist.core;

import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialogList;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.handlers.FaviconHandler;
import org.incendo.cloud.CommandManager;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class PlayerStorageTest{
    
    private final AdvancedServerList<Object> asl = AdvancedServerList.init(new DummyPlugin());
    
    
    
    private static class DummyPlugin implements PluginCore<Object>{
        
        @TempDir
        private Path folder;
        
        @Override
        public void loadEvents(){
        
        }
        
        @Override
        public void unloadEvents(){
        
        }
        
        @Override
        public void loadMetrics(){
        
        }
        
        @Override
        public void loadFaviconHandler(AdvancedServerList<Object> core){
        
        }
        
        @Override
        public void clearFaviconCache(){
        
        }
        
        @Override
        public void downloadLibrary(String groupId, String artifactId, String version){
        
        }
        
        @Override
        public void startScheduler(){
        
        }
        
        @Override
        public AdvancedServerList<Object> core(){
            return null;
        }
        
        @Override
        public Path folderPath(){
            return folder;
        }
        
        @Override
        public PluginLogger pluginLogger(){
            return null;
        }
        
        @Override
        public FaviconHandler<Object> faviconHandler(){
            return null;
        }
        
        @Override
        public CommandManager<CmdSender> commandManager(){
            return null;
        }
        
        @Override
        public String platformInfo(){
            return "";
        }
        
        @Override
        public String loader(){
            return "";
        }
        
        @Override
        public boolean isPluginEnabled(String plugin){
            return false;
        }
        
        @Override
        public boolean isDialogsSupported(){
            return false;
        }
        
        @Override
        public Object createFavicon(BufferedImage image) throws Exception{
            return null;
        }
        
        @Override
        public IDialogList.IDialogListBuilder createDialogListBuilder(String title){
            return null;
        }
        
        @Override
        public IDialog.IDialogBuilder createDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
            return null;
        }
    }
}
