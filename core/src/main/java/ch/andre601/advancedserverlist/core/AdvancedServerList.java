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

package ch.andre601.advancedserverlist.core;

import ch.andre601.advancedserverlist.api.AdvancedServerListAPI;
import ch.andre601.advancedserverlist.api.PlaceholderProvider;
import ch.andre601.advancedserverlist.core.check.UpdateChecker;
import ch.andre601.advancedserverlist.core.commands.CommandHandler;
import ch.andre601.advancedserverlist.core.commands.CommandTypePostprocessor;
import ch.andre601.advancedserverlist.core.compat.maintenance.MaintenancePlaceholder;
import ch.andre601.advancedserverlist.core.file.FileHandler;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.parsing.TextCenterUtil;
import ch.andre601.advancedserverlist.core.placeholders.DatePlaceholder;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.conditions.ProfileConditionParser;
import ch.andre601.advancedserverlist.core.profiles.handlers.PlayerHandler;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AdvancedServerList<F>{
    
    public static <F> AdvancedServerList<F> init(PluginCore<F> plugin, PlaceholderProvider... placeholders){
        List<PlaceholderProvider> providers = new ArrayList<>(Arrays.asList(placeholders));
        providers.add(new DatePlaceholder());
        
        return new AdvancedServerList<>(plugin, providers);
    }
    
    private final PluginCore<F> plugin;
    private final FileHandler fileHandler;
    private final PlayerHandler playerHandler;
    private final TextCenterUtil textCenterUtil;
    
    private static final AdvancedServerListAPI api = AdvancedServerListAPI.get();
    private final ProfileConditionParser parser = ProfileConditionParser.create();
    
    private UpdateChecker updateChecker;
    
    private String version;
    
    private AdvancedServerList(PluginCore<F> plugin, List<PlaceholderProvider> placeholders){
        this.plugin = plugin;
        this.fileHandler = new FileHandler(this);
        this.playerHandler = new PlayerHandler(this);
        this.textCenterUtil = new TextCenterUtil(this);
        
        plugin.pluginLogger().info("Registering internal Placeholders...");
        
        placeholders.forEach(this::addPlaceholder);
        
        if(plugin.isPluginEnabled("Maintenance")){
            addPlaceholder(new MaintenancePlaceholder(this));
        }
        
        resolveVersion();
        plugin().pluginLogger().info("Starting <white>AdvancedServerList v%s</white>...", version);
        load();
    }
    
    public static AdvancedServerListAPI api(){
        return api;
    }
    
    public PluginCore<F> plugin(){
        return plugin;
    }
    
    public FileHandler fileHandler(){
        return fileHandler;
    }
    
    public PlayerHandler playerHandler(){
        return playerHandler;
    }
    
    public String version(){
        return version;
    }
    
    public ProfileConditionParser parser(){
        return parser;
    }
    
    public UpdateChecker updateChecker(){
        return updateChecker;
    }
    
    public TextCenterUtil textCenterUtil(){
        return textCenterUtil;
    }
    
    public void disable(){
        plugin().pluginLogger().info("[-] Saving <white>playercache.json</white> file...");
        playerHandler().save();
        
        if(updateChecker != null){
            plugin().pluginLogger().info("[-] Disabling Update Checker...");
            updateChecker.disable();
        }
        
        plugin().pluginLogger().success("AdvancedServerList disabled!");
    }
    
    public void clearFaviconCache(){
        plugin.clearFaviconCache();
    }
    
    public void clearPlayerCache(){
        playerHandler().clearCache();
    }
    
    private void load(){
        if(!fileHandler().loadConfig()){
            plugin().pluginLogger().failure("Unable to load <white>config.yml</white>! Check previous lines for errors.");
            return;
        }
        
        if(fileHandler().isOldConfig()){
            plugin().pluginLogger().info("[-] Detected old <white>config.yml</white>. Attempting to migrate...");
            if(fileHandler().migrateConfig()){
                plugin().pluginLogger().success("Migration completed successfully!");
            }else{
                plugin().pluginLogger().failure("Couldn't migrate <white>config.yml</white>! Check previous lines for errors.");
                return;
            }
        }
        
        if(fileHandler().getBool(true, "printBanner")){
            printBanner();
        }
        
        plugin().pluginLogger().info("Platform: <white>%s</white>", plugin.platformInfo());
        plugin().pluginLogger().info("");
        
        if(fileHandler().loadProfiles()){
            plugin().pluginLogger().success("Successfully loaded <white>%d</white> profile(s)!", fileHandler().profiles().size());
        }else{
            plugin().pluginLogger().failure("Unable to load profiles! Check previous lines for errors.");
            return;
        }
        
        loadCommands();
        
        plugin.loadEvents();
        playerHandler().load();
        plugin.loadMetrics();
        plugin.startScheduler();
    
        plugin().pluginLogger().success("<green>AdvancedServerList is ready!");
        
        if(fileHandler().getBoolean("checkUpdates"))
            this.updateChecker = new UpdateChecker(this);
    }
    
    private void printBanner(){
        plugin().pluginLogger().info("<#3b90ff>     __      _______ ___");
        plugin().pluginLogger().info("<#3b90ff>    /\\ \\    / ____|_| | |");
        plugin().pluginLogger().info("<#3b90ff>   /  \\ \\  | (_(___ | | |");
        plugin().pluginLogger().info("<#3b90ff>  / /\\ \\ \\  \\___ \\ \\| | |");
        plugin().pluginLogger().info("<#3b90ff> / ____ \\ \\ ____) | | |_|____");
        plugin().pluginLogger().info("<#3b90ff>/_/_/  \\_\\_\\_____/_/|______|_|");
        plugin().pluginLogger().info("");
        
        seasonalText();
    }
    
    private void resolveVersion(){
        try(InputStream stream = getClass().getResourceAsStream("/version.properties")){
            Properties properties = new Properties();
            
            properties.load(stream);
            
            version = properties.getProperty("version");
        }catch(IOException ex){
            version = "UNKNOWN";
        }
    }
    
    private void addPlaceholder(PlaceholderProvider placeholderProvider){
        AdvancedServerList.api().addPlaceholderProvider(placeholderProvider);
    }
    
    private void loadCommands(){
        plugin().loadFaviconHandler(this);
        
        CommandManager<CmdSender> commandManager = plugin().commandManager();
        
        commandManager.parameterInjectorRegistry().registerInjector(AdvancedServerList.class, ParameterInjector.constantInjector(this));
        commandManager.registerCommandPostProcessor(new CommandTypePostprocessor());
        
        AnnotationParser<CmdSender> annotationParser = new AnnotationParser<>(commandManager, CmdSender.class);
        annotationParser.registerBuilderModifier(
            CommandType.class,
            (annotation, builder) -> builder.meta(CommandHandler.COMMAND_TYPE, annotation.value())
        );
        annotationParser.parse(new CommandHandler(commandManager));
        
        for(String option : CommandHandler.PROFILE_OPTIONS){
            commandManager.command(
                commandManager.commandBuilder("advancedserverlist", "asl")
                    .literal("profiles")
                    .literal("set")
                    .required(
                        "profile",
                        StringParser.stringParser(),
                        Description.of("The profile to edit."),
                        SuggestionProvider.suggesting(
                            fileHandler().profiles().stream()
                                .map(ServerListProfile::file)
                                .map(Suggestion::suggestion)
                                .toList()
                        )
                    )
                    .literal(option)
                    .optional("value", descriptor(option), description(option))
                    .commandDescription(Description.of("Sets or resets profile values."))
                    .handler(context -> CommandHandler.handleSet(this, context))
                    .meta(CommandHandler.OPTION, option)
                    .permission(
                        Permission.anyOf(
                            Permission.permission("advancedserverlist.admin"), 
                            Permission.permission("advancedserverlist.command.profiles"),
                            Permission.permission("advancedserverlist.command.profiles.set")
                        )
                    )
            );
        }
        
        plugin().pluginLogger().success("Loaded Command <white>/advancedserverlist</white>!");
    }
    
    private ParserDescriptor<CmdSender, ?> descriptor(String option){
        return switch(option){
            case "playercount.hideplayers", "playercount.hideplayershover", "playercount.extraplayers.enabled",
                "playercount.maxplayers.enabled", "playercount.onlineplayers.enabled" -> BooleanParser.booleanParser();
            case "priority" -> IntegerParser.integerParser();
            default -> StringParser.greedyStringParser();
        };
    }
    
    private Description description(String option){
        return switch(option){
            case "playercount.hideplayers", "playercount.hideplayershover", "playercount.extraplayers.enabled",
                "playercount.maxplayers.enabled", "playercount.onlineplayers.enabled" -> Description.of(
                "Whether this option should be enabled or not. Leave empty to unset it."
            );
            case "priority" -> Description.of("Priority this Profile should have. Leave empty to reset.");
            case "motd", "playercount.hover" -> Description.of(
                "Lines to set. Use \\n or <newline> to set a linebreak. Leave empty to reset."
            );
            default -> Description.of("The Value to use for this option. Leave empty to reset.");
        };
    }
    
    // Adding some seasonal messages
    private void seasonalText(){
        LocalDate date = LocalDate.now();
        
        switch(date.getMonth()){
            case JANUARY -> {
                if(date.getDayOfMonth() == 1){
                    plugin().pluginLogger().info("Happy new Year!");
                    plugin().pluginLogger().info("");
                }
            }
            case JUNE -> {
                plugin().pluginLogger().info("Happy Pride Month!");
                plugin().pluginLogger().info("");
            }
            case AUGUST -> {
                if(date.getDayOfMonth() == 1){
                    plugin().pluginLogger().info("Happy Birthday Switzerland!");
                    plugin().pluginLogger().info("");
                }
            }
            case OCTOBER -> {
                if(date.getDayOfMonth() == 31){
                    plugin().pluginLogger().info("Happy Halloween!");
                    plugin().pluginLogger().info("");
                }
            }
            case DECEMBER -> {
                if(date.getDayOfMonth() == 13){
                    plugin().pluginLogger().info("Happy Birthday Andre_601!");
                    plugin().pluginLogger().info("");
                }else
                if(date.getDayOfMonth() > 23 && date.getDayOfMonth() < 27){
                    plugin().pluginLogger().info("Merry Christmas!");
                    plugin().pluginLogger().info("");
                }
            }
        }
    }
}
