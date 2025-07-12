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

package ch.andre601.advancedserverlist.paper;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.handlers.FaviconHandler;
import ch.andre601.advancedserverlist.paper.commands.PaperCommandManagerInterface;
import ch.andre601.advancedserverlist.paper.listeners.ServerLoadEventListener;
import ch.andre601.advancedserverlist.paper.logging.PaperLogger;
import ch.andre601.advancedserverlist.paper.objects.WorldCache;
import ch.andre601.advancedserverlist.paper.objects.placeholders.PAPIPlaceholders;
import ch.andre601.advancedserverlist.paper.objects.placeholders.PaperPlayerPlaceholders;
import ch.andre601.advancedserverlist.paper.objects.placeholders.PaperServerPlaceholders;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.PaperLibraryManager;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.CachedServerIcon;
import org.incendo.cloud.CommandManager;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

public class PaperCore extends JavaPlugin implements PluginCore<CachedServerIcon>{
    
    private final PluginLogger logger = new PaperLogger(this);
    
    private PaperCommandManagerInterface commandHandler;
    private AdvancedServerList<CachedServerIcon> core;
    private FaviconHandler<CachedServerIcon> faviconHandler = null;
    private PAPIPlaceholders papiPlaceholders = null;
    private WorldCache worldCache = null;
    
    private PaperLibraryManager libraryManager = null;
    
    @Override
    public void onEnable(){
    	try{
            // Required for the Cloud command Framework to function.
    		Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
    	}catch(ClassNotFoundException ex){
    		printOutdatedPaper();
    		return;
    	}
        
        this.commandHandler = new PaperCommandManagerInterface(this);
        this.core = AdvancedServerList.init(this, new PaperPlayerPlaceholders(), new PaperServerPlaceholders(this));
        
        if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            papiPlaceholders = new PAPIPlaceholders(this);
    }
    
    @Override
    public void onDisable(){
        if(papiPlaceholders != null){
            papiPlaceholders.unregister();
            papiPlaceholders = null;
        }
        
        if(worldCache != null)
            worldCache = null;
        
        core().disable();
    }
    
    @Override
    public void loadEvents(){
        new ServerLoadEventListener(this);
    }
    
    @Override
    public void loadMetrics(){
        new Metrics(this, 15584).addCustomChart(new SimplePie("profiles",
            () -> String.valueOf(core.fileHandler().profiles().size())
        ));
    }
    
    @Override
    public void loadFaviconHandler(AdvancedServerList<CachedServerIcon> core){
        faviconHandler = new FaviconHandler<>(core);
    }
    
    @Override
    public void clearFaviconCache(){
        if(faviconHandler == null)
            return;
        
        faviconHandler.cleanCache();
    }
    
    @Override
    public void downloadLibrary(String groupId, String artifactId, String version){
        if(libraryManager == null){
            libraryManager = new PaperLibraryManager(this);
            libraryManager.addRepository("https://repo.papermc.io/repository/maven-public");
        }
        
        Library lib = Library.builder()
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .resolveTransitiveDependencies(true)
            .build();
        
        libraryManager.loadLibrary(lib);
    }
    
    @Override
    public void startScheduler(){
        // Unused
    }
    
    @Override
    public AdvancedServerList<CachedServerIcon> core(){
        return core;
    }
    
    @Override
    public Path folderPath(){
        return getDataFolder().toPath();
    }
    
    @Override
    public PluginLogger pluginLogger(){
        return logger;
    }
    
    @Override
    public FaviconHandler<CachedServerIcon> faviconHandler(){
        if(faviconHandler == null)
            faviconHandler = new FaviconHandler<>(core);
        
        return faviconHandler;
    }
    
    @Override
    public CommandManager<CmdSender> commandManager(){
        return commandHandler.commandHandler();
    }
    
    @Override
    public String platformInfo(){
        try{
            Class.forName("io.papermc.paper.ServerBuildInfo");
            return buildInfo();
        }catch(ClassNotFoundException ignored){
            // Old Paper versions (Before 16th of May 2024)
            return version();
        }
    }
    
    @Override
    public String loader(){
        return "paper";
    }
    
    @Override
    public boolean isPluginEnabled(String plugin){
        return Bukkit.getPluginManager().isPluginEnabled(plugin);
    }
    
    @Override
    public boolean isDialogsSupported(){
        try{
            Class.forName("io.papermc.paper.dialog.Dialog");
            return true;
        }catch(ClassNotFoundException ignored){
            return false;
        }
    }
    
    @Override
    public CachedServerIcon createFavicon(BufferedImage image) throws Exception{
        return Bukkit.loadServerIcon(image);
    }
    
    
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public DialogLike createDialog(ServerListProfile profile){
        DialogBase.Builder builder = DialogBase.builder(Component.text(profile.file()))
            .body(List.of(
                DialogBody.plainMessage(
                    ComponentParser.text("""
                        <gold><bold>WARNING!</bold></gold>
                        Dialog-based Profiles Editing is considered <u>experimental!</u>
                        
                        You may encounter bugs and glitches.
                        <u>Always create backups before using this feature!</u>
                        """).toComponent()
                ),
                DialogBody.plainMessage(
                    ComponentParser.text("""
                        <b>Profiles</b>
                        Below can you find the Main Settings alongside any <aqua>profiles</aqua> entry of this file.
                        """).toComponent()
                )
            ));
        
        List<Dialog> dialogs = new ArrayList<>();
        dialogs.add(
            createProfileDialog(
                profile.file(),
                "Main Settings",
                profile.defaultProfile(),
                profile.priority(),
                profile.condition(),
                -1
            )
        );
        
        for(int i = 0; i < profile.profiles().size(); i++){
            dialogs.add(
                createProfileDialog(
                    profile.file(),
                    String.format("Profiles <aqua>#%d</aqua>", i + 1),
                    profile.profiles().get(i),
                    null,
                    null,
                    i
                )
            );
        }
        
        RegistrySet<Dialog> dialogRegistry = RegistrySet.valueSet(RegistryKey.DIALOG, dialogs);
        
        return Dialog.create(
            factory -> factory.empty().base(builder.build())
                .type(DialogType.dialogList(dialogRegistry).columns(3).build())
        );
    }
    
    public WorldCache worldCache(){
        if(worldCache != null)
            return worldCache;
        
        return (worldCache = new WorldCache());
    }
    
    public int playersOnline(World world){
        List<? extends Player> players = new ArrayList<>(world == null ? getServer().getOnlinePlayers() : world.getPlayers());
        
        players.removeIf(player -> {
            for(MetadataValue metadata : player.getMetadata("vanished")){
                if(metadata.asBoolean())
                    return true;
            }
            
            return false;
        });
        
        return players.size();
    }
    
    private String buildInfo(){
        ServerBuildInfo info = ServerBuildInfo.buildInfo();
        
        String name = info.brandName();
        String id = info.brandId().asString();
        String version = info.minecraftVersionName();
        
        OptionalInt build = info.buildNumber();
        if(build.isEmpty())
            return String.format("%s (ID: %s, Version: %s)", name, id, version);
        
        return String.format("%s (ID: %s, Version: %s, Build: %d)", name, id, version, build.getAsInt());
    }
    
    private String version(){
        return getServer().getName() + " " + getServer().getVersion();
    }
    
    private void printOutdatedPaper(){
    	pluginLogger().warn("================================================================");
    	pluginLogger().warn("OUTDATED PAPER VERSION DETECTED!");
    	pluginLogger().warn("");
    	pluginLogger().warn("AdvancedServerList detected an outdated Paper version.");
    	pluginLogger().warn("The plugin requires at least 1.20.6 of Paper due to required");
    	pluginLogger().warn("classes only existing within this version onwards.");
    	pluginLogger().warn("");
    	pluginLogger().warn("Please use at least Paper 1.20.6 or downgrade AdvancedServerList");
    	pluginLogger().warn("to v5.4.1.");
    	pluginLogger().warn("================================================================");
    }
    
    @SuppressWarnings("UnstableApiUsage")
    private Dialog createProfileDialog(String file, String title, ProfileEntry entry, Integer priority, String condition, int profilesPosition){
        DialogBase.Builder builder = DialogBase.builder(ComponentParser.text(title).toComponent())
            .body(Collections.singletonList(
                DialogBody.plainMessage(ComponentParser.text("""
                    <gold><bold>WARNING!</bold></gold>
                    Dialog-based Profiles Editing is considered <u>experimental!</u>
                    
                    You may encounter bugs and glitches.
                    <u>Always create backups before using this feature!</u>
                    """).toComponent())
            ))
            .afterAction(DialogBase.DialogAfterAction.CLOSE);
        
        List<DialogInput> inputs = new ArrayList<>();
        
        if(priority != null){
            inputs.add(
                DialogInput.numberRange("priority", Component.text("Priority"), -100.0f, 100.0f)
                    .initial((float)priority)
                    .step(1.0f)
                    .build()
            );
        }
        
        if(profilesPosition < 0){
            inputs.add(
                DialogInput.text("condition", Component.text("Condition"))
                    .initial(condition == null ? "" : condition)
                    .maxLength(200)
                    .build()
            );
        }
        
        inputs.add(
            DialogInput.text("motd", Component.text("MOTD"))
                .initial(String.join("\n", entry.motd()))
                .multiline(TextDialogInput.MultilineOptions.create(2, 25))
                .maxLength(100)
                .build()
        );
        
        inputs.add(
            DialogInput.text("favicon", Component.text("Favicon"))
                .initial(entry.favicon() == null ? "" : entry.favicon())
                .maxLength(50)
                .build()
        );
        
        inputs.add(
            DialogInput.bool("hidePlayers", Component.text("Hide Players?"))
                .initial(entry.hidePlayersEnabled().getOrDefault(false))
                .build()
        );
        
        inputs.add(
            DialogInput.bool("hidePlayersHover", Component.text("Hide Players Hover?"))
                .initial(entry.hidePlayersHoverEnabled().getOrDefault(false))
                .build()
        );
        
        inputs.add(
            DialogInput.text("hover", Component.text("Player Count Hover"))
                .initial(String.join("\n", entry.players()))
                .multiline(TextDialogInput.MultilineOptions.create(10, 125))
                .maxLength(200)
                .build()
        );
        
        inputs.add(
            DialogInput.text("text", Component.text("Player Count Text"))
                .initial(entry.playerCountText() == null ? "" : entry.playerCountText())
                .maxLength(200)
                .build()
        );
        
        inputs.add(
            DialogInput.bool("extraPlayersEnabled", Component.text("Extra Players enabled?"))
                .initial(entry.extraPlayersEnabled().getOrDefault(false))
                .build()
        );
        
        inputs.add(
            DialogInput.text("extraPlayersCount", Component.text("Extra Players Count"))
                .initial(entry.extraPlayersCount() == null ? "" : entry.extraPlayersCount())
                .maxLength(200)
                .build()
        );
        
        inputs.add(
            DialogInput.bool("maxPlayersEnabled", Component.text("Max Players enabled?"))
                .initial(entry.maxPlayersEnabled().getOrDefault(false))
                .build()
        );
        
        inputs.add(
            DialogInput.text("maxPlayersCount", Component.text("Max Players Count"))
                .initial(entry.maxPlayersCount() == null ? "" : entry.maxPlayersCount())
                .maxLength(200)
                .build()
        );
        
        inputs.add(
            DialogInput.bool("onlinePlayersEnabled", Component.text("Online Players enabled?"))
                .initial(entry.onlinePlayersEnabled().getOrDefault(false))
                .build()
        );
        
        inputs.add(
            DialogInput.text("onlinePlayersCount", Component.text("Online Players Count"))
                .initial(entry.onlinePlayersCount() == null ? "" : entry.onlinePlayersCount())
                .maxLength(200)
                .build()
        );
        
        builder.inputs(inputs);
        
        return Dialog.create(
            factory -> factory.empty()
                .base(builder.build())
                .type(DialogType.confirmation(
                    ActionButton.builder(ComponentParser.text("<green>Save changes").toComponent())
                        .action(DialogAction.customClick(
                            (view, audience) -> saveProfile(file, view, audience, profilesPosition),
                            ClickCallback.Options.builder().build()))
                        .tooltip(ComponentParser.text("""
                            <grey>Save your changes.
                            
                            <grey>Requires you to use <aqua>/asl reload</aqua> to apply changes.""").toComponent())
                        .build(),
                    ActionButton.builder(ComponentParser.text("<red>Cancel").toComponent())
                        .tooltip(ComponentParser.text("""
                            <grey>Close this window without saving changes.""").toComponent())
                        .build()
                ))
        );
    }
    
    @SuppressWarnings("UnstableApiUsage")
    private void saveProfile(String filename, DialogResponseView view, Audience audience, int profilesPosition){
        ServerListProfile slp = core().fileHandler().profile(filename);
        if(slp == null){
            audience.sendMessage(
                ComponentParser.textFormatted("<red>Unable to save changes. Couldn't find file <white>%s</white>", filename)
                    .toComponent()
            );
            return;
        }
        
        Float priority = view.getFloat("priority");
        String condition = view.getText("condition");
        
        String motd = view.getText("motd");
        String favicon = view.getText("favicon");
        
        NullBool hidePlayers = NullBool.resolve(view.getBoolean("hidePlayers"));
        NullBool hidePlayersHover = NullBool.resolve(view.getBoolean("hidePlayersHover"));
        
        String hover = view.getText("hover");
        String playerCountText = view.getText("text");
        
        NullBool extraPlayersEnabled = NullBool.resolve(view.getBoolean("extraPlayersEnabled"));
        String extraPlayersCount = view.getText("extraPlayersCount");
        
        NullBool maxPlayersEnabled = NullBool.resolve(view.getBoolean("maxPlayersEnabled"));
        String maxPlayersCount = view.getText("maxPlayersCount");
        
        NullBool onlinePlayersEnabled = NullBool.resolve(view.getBoolean("onlinePlayersEnabled"));
        String onlinePlayersCount = view.getText("onlinePlayersCount");
        
        ProfileEntry.Builder builder = ProfileEntry.empty().builder();
        if(motd != null && !motd.isEmpty())
            builder.motd(List.of(motd.split("\n")));
        
        if(favicon != null && !favicon.isEmpty())
            builder.favicon(favicon);
        
        builder.hidePlayersEnabled(hidePlayers);
        builder.hidePlayersHoverEnabled(hidePlayersHover);
        
        if(hover != null && !hover.isEmpty())
            builder.players(List.of(hover.split("\n")));
        
        if(playerCountText != null && !playerCountText.isEmpty())
            builder.playerCountText(playerCountText);
        
        builder.extraPlayersEnabled(extraPlayersEnabled);
        if(extraPlayersCount != null && !extraPlayersCount.isEmpty())
            builder.extraPlayersCount(extraPlayersCount);
        
        builder.maxPlayersEnabled(maxPlayersEnabled);
        if(maxPlayersCount != null && !maxPlayersCount.isEmpty())
            builder.maxPlayersCount(maxPlayersCount);
        
        builder.onlinePlayersEnabled(onlinePlayersEnabled);
        if(onlinePlayersCount != null && !onlinePlayersCount.isEmpty())
            builder.onlinePlayersCount(onlinePlayersCount);
        
        ProfileEntry entry = slp.defaultProfile();
        List<ProfileEntry> entries = slp.profiles();
        if(profilesPosition >= 0){
            if(profilesPosition >= entries.size()){
                entries.add(builder.build());
            }else{
                entries.set(profilesPosition, builder.build());
            }
        }else{
            entry = builder.build();
        }
        
        float newPriority = priority == null ? slp.priority() : priority;
        
        if(core().fileHandler().saveProfile(filename, (int)newPriority, condition, entries, entry)){
            audience.sendMessage(ComponentParser.text("<green>Changes saved successfully!").toComponent());
        }else{
            audience.sendMessage(ComponentParser.text("<red>Error while saving changes. Check console for details").toComponent());
        }
    }
}
