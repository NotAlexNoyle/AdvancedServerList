/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Andre_601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ch.andre601.advancedserverlist.core.interfaces.core;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialogList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.handlers.FaviconHandler;
import org.incendo.cloud.CommandManager;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public interface PluginCore<F>{
    
    void loadEvents();
    
    void loadMetrics();
    
    void loadFaviconHandler(AdvancedServerList<F> core);
    
    void clearFaviconCache();
    
    void downloadLibrary(String groupId, String artifactId, String version);
    
    void startScheduler();
    
    AdvancedServerList<F> core();
    
    Path folderPath();
    
    PluginLogger pluginLogger();
    
    FaviconHandler<F> faviconHandler();
    
    CommandManager<CmdSender> commandManager();
    
    String platformInfo();
    
    String loader();
    
    boolean isPluginEnabled(String plugin);
    
    boolean isDialogsSupported();
    
    F createFavicon(BufferedImage image) throws Exception;
    
    IDialogList.IDialogListBuilder createDialogListBuilder(String title);
    
    IDialog.IDialogBuilder createDialogBuilder(String title, ServerListProfile profile, int profilesPosition);
    
    default boolean isDebugEnabled(){
        if(core() == null)
            return false;
        
        return core().fileHandler().getBoolean("debug");
    }
}
