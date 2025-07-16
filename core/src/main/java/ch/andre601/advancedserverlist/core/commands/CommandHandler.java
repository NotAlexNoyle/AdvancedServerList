/*
 * MIT License
 *
 * Copyright (c) 2022-2025 Andre_601
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
 */

package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import ch.andre601.advancedserverlist.core.migration.minimotd.MiniMOTDConfigMigrator;
import ch.andre601.advancedserverlist.core.migration.serverlistplus.SLPConfigMigrator;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.profile.ProfileSerializer;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Command("advancedserverlist|asl")
@CommandDescription("Main command of the plugin.")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public class CommandHandler{
    
    public static final CloudKey<CommandType.Type> COMMAND_TYPE = CloudKey.of("command_type", CommandType.Type.class);
    public static final CloudKey<String> OPTION = CloudKey.of("profile_option", String.class);
    public static final List<String> PROFILE_OPTIONS = List.of(
        "priority",
        "condition",
        "motd",
        "favicon",
        "playercount.hideplayers",
        "playercount.hideplayershover",
        "playercount.hover",
        "playercount.text",
        "playercount.extraplayers.enabled",
        "playercount.extraplayers.amount",
        "playercount.maxplayers.enabled",
        "playercount.maxplayers.amount",
        "playercount.onlineplayers.enabled",
        "playercount.onlineplayers.amount"
    );
    
    public static void handleSet(AdvancedServerList<?> core, CommandContext<CmdSender> context){
        CmdSender sender = context.sender();
        String profile = context.get("profile");
        String value = getOptionValue(context.getOrDefault("value", null));
        String option = context.command().commandMeta().getOrDefault(OPTION, "");
        
        ServerListProfile slp = profile(core, profile);
        if(slp == null){
            sender.sendErrorMsg("<red>No profile with name</red> %s <red>found!", profile);
            return;
        }
        
        int priority = slp.priority();
        String condition = slp.condition();
        ProfileEntry entry = slp.defaultProfile();
        ProfileEntry.Builder builder = slp.defaultProfile().builder();
        
        List<String> oldValues = new ArrayList<>();
        List<String> newValues = new ArrayList<>();
        
        switch(option.toLowerCase(Locale.ROOT)){
            case "priority" -> {
                oldValues.add(String.valueOf(slp.priority()));
                
                if(value == null || value.isEmpty()){
                    priority = 0;
                    break;
                }
                
                try{
                    priority = Integer.parseInt(value);
                    
                    newValues.add(String.valueOf(priority));
                }catch(NumberFormatException ex){
                    sender.sendErrorMsg("<red>Invalid value provided. Expected number but got</red> %s", value);
                    return;
                }
            }
            case "condition" -> {
                oldValues.add(slp.condition());
                
                if(value == null || value.isEmpty()){
                    condition = "";
                    break;
                }
                
                condition = value;
                newValues.add(value);
            }
            case "motd" -> {
                oldValues.addAll(entry.motd());
                
                if(value == null || value.isEmpty()){
                    builder.motd(Collections.emptyList());
                    break;
                }
                
                String[] lines = value.split("\\\\n|<newline>");
                if(lines.length == 0){
                    builder.motd(Collections.emptyList());
                    break;
                }
                
                if(lines.length > 1){
                    if(lines.length > 2)
                        sender.sendPrefixedMsg("<gold>Found %d Lines for MOTD. Only using first 2 lines...", lines.length);
                        
                    builder.motd(Arrays.asList(lines));
                }else{
                    builder.motd(Collections.singletonList(lines[0]));
                }
                
                newValues.addAll(Arrays.asList(lines));
            }
            case "favicon" -> {
                oldValues.add(entry.favicon());
                
                if(value == null || value.isEmpty()){
                    builder.favicon("");
                    break;
                }
                
                builder.favicon(value);
                
                newValues.add(value);
            }
            case "playercount.hideplayers" -> {
                if(!entry.hidePlayersEnabled().isNotSet())
                    oldValues.add(String.valueOf(entry.hidePlayersEnabled().getOrDefault(false)));
                
                if(value == null || value.isEmpty()){
                    builder.hidePlayersEnabled(NullBool.NOT_SET);
                    break;
                }
                
                NullBool bool = NullBool.resolve(Boolean.parseBoolean(value));
                builder.hidePlayersEnabled(bool);
                
                newValues.add(String.valueOf(bool.getOrDefault(false)));
            }
            case "playercount.hideplayershover" -> {
                if(!entry.hidePlayersHoverEnabled().isNotSet())
                    oldValues.add(String.valueOf(entry.hidePlayersHoverEnabled().getOrDefault(false)));
                
                if(value == null || value.isEmpty()){
                    builder.hidePlayersHoverEnabled(NullBool.NOT_SET);
                    break;
                }
                
                NullBool bool = NullBool.resolve(Boolean.parseBoolean(value));
                builder.hidePlayersHoverEnabled(bool);
                
                newValues.add(String.valueOf(bool.getOrDefault(false)));
            }
            case "playercount.hover" -> {
                oldValues.addAll(entry.players());
                
                if(value == null || value.isEmpty()){
                    builder.players(Collections.emptyList());
                    break;
                }
                
                String[] lines = value.split("\\\\n|<newline>");
                if(lines.length == 0){
                    builder.players(Collections.emptyList());
                    break;
                }
                
                builder.players(Arrays.asList(lines));
                
                newValues.addAll(Arrays.asList(lines));
            }
            case "playercount.text" -> {
                if(!entry.playerCountText().isEmpty())
                    oldValues.add(entry.playerCountText());
                
                if(value == null || value.isEmpty()){
                    builder.playerCountText("");
                    break;
                }
                
                builder.playerCountText(value);
                
                newValues.add(value);
            }
            case "playercount.extraplayers.enabled" -> {
                if(!entry.extraPlayersEnabled().isNotSet())
                    oldValues.add(String.valueOf(entry.extraPlayersEnabled().getOrDefault(false)));
                
                if(value == null || value.isEmpty()){
                    builder.extraPlayersEnabled(NullBool.NOT_SET);
                    break;
                }
                
                NullBool bool = NullBool.resolve(Boolean.parseBoolean(value));
                builder.extraPlayersEnabled(bool);
                
                newValues.add(String.valueOf(bool.getOrDefault(false)));
            }
            case "playercount.extraplayers.amount" -> {
                if(!entry.extraPlayersCount().isEmpty())
                    oldValues.add(entry.extraPlayersCount());
                
                if(value == null || value.isEmpty()){
                    builder.extraPlayersCount("");
                    break;
                }
                
                builder.extraPlayersCount(value);
                
                newValues.add(value);
            }
            case "playercount.maxplayers.enabled" -> {
                if(!entry.maxPlayersEnabled().isNotSet())
                    oldValues.add(String.valueOf(entry.maxPlayersEnabled().getOrDefault(false)));
                
                if(value == null || value.isEmpty()){
                    builder.maxPlayersEnabled(NullBool.NOT_SET);
                    break;
                }
                
                NullBool bool = NullBool.resolve(Boolean.parseBoolean(value));
                builder.maxPlayersEnabled(bool);
                
                newValues.add(String.valueOf(bool.getOrDefault(false)));
            }
            case "playercount.maxplayers.amount" -> {
                if(!entry.maxPlayersCount().isEmpty())
                    oldValues.add(entry.extraPlayersCount());
                
                if(value == null || value.isEmpty()){
                    builder.maxPlayersCount("");
                    break;
                }
                
                builder.maxPlayersCount(value);
                
                newValues.add(value);
            }
            case "playercount.onlineplayers.enabled" -> {
                if(!entry.onlinePlayersEnabled().isNotSet())
                    oldValues.add(String.valueOf(entry.onlinePlayersEnabled().getOrDefault(false)));
                
                if(value == null || value.isEmpty()){
                    builder.onlinePlayersEnabled(NullBool.NOT_SET);
                    break;
                }
                
                NullBool bool = NullBool.resolve(Boolean.parseBoolean(value));
                builder.onlinePlayersEnabled(bool);
                
                newValues.add(String.valueOf(bool.getOrDefault(false)));
            }
            case "playercount.onlineplayers.amount" -> {
                if(!entry.onlinePlayersCount().isEmpty())
                    oldValues.add(entry.onlinePlayersCount());
                
                if(value == null || value.isEmpty()){
                    builder.onlinePlayersCount("");
                    break;
                }
                
                builder.onlinePlayersCount(value);
                
                newValues.add(value);
            }
            default -> {
                sender.sendErrorMsg("<red>Unknown option</red> %s<red>!", option);
                return;
            }
        }
        
        if(core.fileHandler().saveProfile(profileName(profile), priority, condition, slp.profiles(), builder.build())){
            if(sender.isPlayer()){
                sender.sendPrefixedMsg(
                    "<green>Successfully updated <white>[<grey><hover:show_text:\"%s\">%s</hover></grey>]</white>!",
                    changeValueHover(oldValues, newValues),
                    option
                );
            }else{
                sender.sendPrefixedMsg("<green>Successfully updated <white>[<grey>%s</grey>]</white>:", option);
                
                if(oldValues.isEmpty()){
                    sender.sendMsg("<red>-</red> <white><i>Not set</i></white>");
                }else{
                    oldValues.forEach(oldValue -> sender.sendMsg("<red>-</red> %s", oldValue));
                }
                
                sender.sendMsg();
                
                if(newValues.isEmpty()){
                    sender.sendMsg("<green>+</green> <white><i>Reset to default</i></white>");
                }else{
                    newValues.forEach(newValue -> sender.sendMsg("<green>+</green> %s", newValue));
                }
                
                sender.sendMsg();
            }
            
            sender.sendPrefixedMsg(
                "<green>Use <white><click:suggest_command:/asl reload>/asl reload</click></white> to apply changes."
            );
        }else{
            sender.sendErrorMsg("<red>Encountered an IOException while trying to save file</red> %s<red>.", profile);
            sender.sendErrorMsg("<red>Check console for details.");
            
            core.plugin().pluginLogger().warn("Encountered IOException while saving file <white>%s.yml</white>", profile);
        }
    }
    
    private static ServerListProfile profile(AdvancedServerList<?> core, String name){
        return core.fileHandler().profiles().stream()
            .filter(p -> p.file().equalsIgnoreCase(profileName(name)))
            .findFirst()
            .orElse(null);
    }
    
    private static String profileName(String name){
        if(name == null || name.isEmpty())
            return "";
        
        String filename = name.toLowerCase(Locale.ROOT);
        
        return filename.endsWith(".yml") ? filename : filename + ".yml";
    }
    
    private static String changeValueHover(List<String> oldValues, List<String> newValues){
        return """
            <red>-</red> %s<reset>
            
            <green>+</green> %s""".formatted(
            oldValues.isEmpty() ? "<i>Not set</i>" : String.join("<reset>\n<red>-</red> ", oldValues),
            newValues.isEmpty() ? "<i>Not set</i>" : String.join("<reset>\n<green>+</green> ", newValues)
        );
    }
    
    private static String getOptionValue(Object obj){
        return switch(obj){
            case null -> "";
            case Boolean bool -> Boolean.toString(bool);
            case Integer integer -> Integer.toString(integer);
            case String str -> str;
            default -> obj.toString();
        };
    }
    
    private final CommandManager<CmdSender> commandManager;
    private final MinecraftHelp<CmdSender> help;
    
    public CommandHandler(CommandManager<CmdSender> commandManager){
        this.commandManager = commandManager;
        this.help = MinecraftHelp.<CmdSender>builder()
            .commandManager(commandManager)
            .audienceProvider(CmdSender::audience)
            .commandPrefix("/advancedserverlist help")
            .colors(MinecraftHelp.helpColors(
                NamedTextColor.WHITE, NamedTextColor.AQUA, NamedTextColor.WHITE, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY
            ))
            .build();
    }
    
    @Command("help [query]")
    @CommandDescription("Displays the commands available for AdvancedServerList")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.help"})
    @CommandType(CommandType.Type.ALL)
    public void help(
        CmdSender sender,
        @Nullable @Argument(description = "Query to receive info of a specific command.", suggestions = "help") @Greedy String query
    ){
        this.help.queryCommands(query == null ? "" : query, sender);
    }
    
    @Command("reload")
    @CommandDescription("Reloads the config.yml and all existing profiles.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.reload"})
    @CommandType(CommandType.Type.ALL)
    public void reload(CmdSender sender, AdvancedServerList<?> core){
        sender.sendPrefixedMsg("Reloading plugin...");
        
        if(core.fileHandler().reloadConfig()){
            sender.sendPrefixedMsg("<green>Reloaded </green>config.yml<green>!");
        }else{
            sender.sendErrorMsg("<red>Error while reloading </red>config.yml<red>.");
            sender.sendErrorMsg("<red>Check console for details.");
        }
        
        if(core.fileHandler().reloadProfiles()){
            sender.sendPrefixedMsg("<green>(Re)loaded </green>%d<green> Profile(s)!", core.fileHandler().profiles().size());
        }else{
            sender.sendErrorMsg("<red>Error while (re)loading profiles.");
            sender.sendErrorMsg("<red>Check console for details.");
        }
        
        sender.sendPrefixedMsg("<green>Reload complete!");
    }
    
    @Command("clearcache")
    @CommandDescription("Clears the Favicon and Player cache of the plugin.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.clearcache"})
    @CommandType(CommandType.Type.ALL)
    public void clearCache(CmdSender sender, AdvancedServerList<?> core){
        sender.sendPrefixedMsg("Clearing cache...");
        
        core.clearFaviconCache();
        sender.sendPrefixedMsg("Cleared Favicon cache.");
        
        core.clearPlayerCache();
        sender.sendPrefixedMsg("Cleared Player cache.");
        
        sender.sendPrefixedMsg("<green>Cache clearing complete!");
    }
    
    @Command("migrate <plugin>")
    @CommandDescription("Migrates Profiles from other plugins to itself.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.migrate"})
    @CommandType(CommandType.Type.ALL)
    public void migrate(
        CmdSender sender,
        AdvancedServerList<?> core,
        @NonNull @Argument(description = "The plugin to migrate from.") Plugin plugin
    ){
        if(plugin == Plugin.SERVERLISTPLUS){
            if(!core.plugin().isPluginEnabled("ServerListPlus")){
                sender.sendErrorMsg("<red>ServerListPlus is not enabled.");
                sender.sendErrorMsg("<red>The plugin is required for the migration to work.");
                return;
            }
            
            sender.sendPrefixedMsg("Migrating from ServerListPlus...");
            
            int migrated = SLPConfigMigrator.migrate(core, sender);
            if(migrated == 0){
                sender.sendErrorMsg("<red>Unable to migrate any profiles from ServerListPlus.");
                sender.sendErrorMsg("<red>Check console for details.");
            }else{
                sender.sendPrefixedMsg("<green>Successfully migrated </green>%d<green> Profile(s)", migrated);
            }
        }else
        if(plugin == Plugin.MINIMOTD){
            sender.sendPrefixedMsg("Migrating from MiniMOTD...");
            
            if(MiniMOTDConfigMigrator.migrate(core, sender)){
                sender.sendPrefixedMsg("<green>Successfully migrated from MiniMOTD!");
            }else{
                sender.sendErrorMsg("<red>Unable to migrate from MiniMOTD.");
                sender.sendErrorMsg("<red>Check console for details.");
            }
        }else{
            sender.sendErrorMsg("<red>Received invalid Plugin name </red>%s<red>.", plugin);
            sender.sendMsg("<grey>Supported plugins [Click for command]:");
            sender.sendMsg();
            sender.sendMsg(" <grey>-</grey> <click:suggest_command:/asl migrate serverlistplus>ServerListPlus</click>");
            sender.sendMsg(" <grey>-</grey> <click:suggest_command:/asl migrate minimotd>MiniMOTD</click>");
        }
    }
    
    @Command("profiles list")
    @CommandDescription("Lists all available profiles with their priority, conditions and if they are valid.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.list"})
    @CommandType(CommandType.Type.ALL)
    public void list(CmdSender sender, AdvancedServerList<?> core){
        sender.sendMsg();
        sender.sendPrefixedMsg("Available Profiles");
        sender.sendMsg("<dark_grey>│");
        
        List<ServerListProfile> profiles = core.fileHandler().profiles().stream()
                .sorted(Comparator.comparingInt(ServerListProfile::priority).reversed())
                .toList();
        
        if(profiles.isEmpty()){
            sender.sendMsg("<dark_grey>└─</dark_grey> <i>No profiles found</i>");
        }else{
            for(int i = 0; i < profiles.size(); i++){
                sender.sendMsg(
                    "<dark_grey>%s─</dark_grey> <aqua>" +
                    "<hover:show_text:\"%s\">" +
                    "<click:suggest_command:/asl profiles info %s>" +
                    "%s" +
                    "</click>" +
                    "</hover>" +
                    "</aqua>",
                    (i + 1) == profiles.size() ? "└" : "├",
                    hover(profiles.get(i)),
                    profiles.get(i).file(),
                    profiles.get(i).file()
                );
            }
        }
        if(sender.isPlayer()){
            sender.sendMsg();
            sender.sendMsg("<grey>[<white>Hover a name for details. Click for a command</white>]</grey>");
        }
        sender.sendMsg();
    }
    
    @Command("profiles info <profile>")
    @CommandDescription("Provides info about a profile.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.info"})
    @CommandType(CommandType.Type.PLAYER_ONLY)
    public void info(
        CmdSender sender,
        AdvancedServerList<?> core,
        @NonNull @Argument(description = "Name of the profile to view.", suggestions = "profiles") String profile
    ){
        if(profile.isEmpty()){
            sender.sendErrorMsg("<red>Insufficient arguments.");
            sender.sendErrorMsg("<red>Please provide a proper profile name.");
            return;
        }
        
        ServerListProfile slp = profile(core, profile);
        
        if(slp == null){
            sender.sendErrorMsg("<red>Unable to find profile with name </red>%s<red>.", profile);
            sender.sendErrorMsg("<red>Make sure you typed it correctly.");
            return;
        }
        
        ProfileOptionsSender optionsSender = new ProfileOptionsSender(slp.file());
        
        optionsSender
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Priority", slp.priority())
                    .option("priority")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Condition", slp.condition())
                    .option("condition")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Profiles")
                    .color(slp.profiles().isEmpty() ? ProfileOptionsSender.ColorName.RED : ProfileOptionsSender.ColorName.GREEN)
            );
        
        if(!slp.profiles().isEmpty()){
            for(int i = 0; i < slp.profiles().size(); i++){
                optionsSender.append(
                    ProfileOptionsSender.OptionStringBuilder.of("#" + (i + 1), profileHover(slp.profiles().get(i)))
                        .prefix("│    %s─".formatted((i + 1) == slp.profiles().size() ? "└" : "├"))
                        .color(ProfileOptionsSender.ColorName.AQUA)
                );
            }
        }
        
        ProfileEntry entry = slp.defaultProfile();
        
        optionsSender
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("MOTD", entry.motd())
                    .option("motd")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Favicon", entry.favicon())
                    .option("favicon")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Player count")
                    .prefix("└─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Hide Player count?", entry.hidePlayersEnabled())
                    .prefix("     ├─")
                    .option("playercount.hideplayers")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Hide Player count hover?", entry.hidePlayersHoverEnabled())
                    .prefix("     ├─")
                    .option("playercount.hideplayershover")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Player count hover", entry.players())
                    .prefix("     ├─")
                    .option("playercount.hover")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Player count text", entry.playerCountText())
                    .prefix("     ├─")
                    .option("playercount.text")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Extra Players")
                    .prefix("     ├─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Enabled?", entry.extraPlayersEnabled())
                    .prefix("     │    ├─")
                    .option("playercount.extraplayers.enabled")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Amount", entry.extraPlayersCount())
                    .prefix("     │    └─")
                    .option("playercount.extraplayers.amount")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Max Players")
                    .prefix("     ├─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Enabled?", entry.maxPlayersEnabled())
                    .prefix("     │    ├─")
                    .option("playercount.maxplayers.enabled")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Amount", entry.maxPlayersCount())
                    .prefix("     │    └─")
                    .option("playercount.maxplayers.amount")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Online Players")
                    .prefix("     └─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Enabled?", entry.onlinePlayersEnabled())
                    .prefix("          ├─")
                    .option("playercount.onlineplayers.enabled")
            )
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Amount", entry.onlinePlayersCount())
                    .prefix("          └─")
                    .option("playercount.onlineplayers.amount")
            )
            .send(sender);
    }
    
    @Command("profiles add <name>")
    @CommandDescription("Creates a new profile with default values applied.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.add"})
    @CommandType(CommandType.Type.ALL)
    public void add(
        CmdSender sender,
        AdvancedServerList<?> core,
        @NonNull @Argument(description = "Name of the profile to create.") String name
    ){
        if(name.isEmpty()){
            sender.sendErrorMsg("<red>Insufficient arguments.");
            sender.sendErrorMsg("<red>Please provide a proper file name.");
            return;
        }
        
        if(profile(core, name) != null){
            sender.sendErrorMsg("<red>A profile with name </red>%s<red> already exists.", name);
            return;
        }
        
        String fileName = profileName(name);
        
        if(core.fileHandler().createFile(fileName)){
            sender.sendPrefixedMsg("<green>Successfully created </green>%s<green>!", fileName);
            sender.sendPrefixedMsg("<green>Use <white><click:suggest_command:/asl reload>/asl reload</click></white> to load the file.");
        }else{
            sender.sendErrorMsg("<red>Error while creating new profile.");
            sender.sendErrorMsg("<red>Check console for details.");
        }
    }
    
    @Command("profiles copy <profile> <name>")
    @CommandDescription("Creates a copy of an existing profile.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.copy"})
    @CommandType(CommandType.Type.ALL)
    public void copy(
        CmdSender sender,
        AdvancedServerList<?> core,
        @NonNull @Argument(description = "Name of the profile to copy.", suggestions = "profiles") String profile,
        @NonNull @Argument(description = "Name of the copy.") String name
    ){
        if(profile.isEmpty() || name.isEmpty()){
            sender.sendErrorMsg("<red>Insufficient arguments.");
            sender.sendErrorMsg("<red>Please provide a profile name and name for the copy.");
            return;
        }
        
        String profileName = profileName(profile);
        String copyName = profileName(name);
        
        ServerListProfile targetProfile = profile(core, profile);
        boolean copyExists = profile(core, name) != null;
        
        if(targetProfile == null){
            sender.sendErrorMsg("<red>No profile with name </red>%s<red> exists.", profile);
            return;
        }
        
        if(copyExists){
            sender.sendErrorMsg("<red>A file with name </red>%<red> already exists!", copyName);
            return;
        }
        
        Path profilePath = core.plugin().folderPath().resolve("profiles").resolve(copyName);
        if(!Files.exists(profilePath)){
            try{
                Files.createFile(profilePath);
            }catch(IOException ex){
                sender.sendErrorMsg("<red>Encountered IOException while creating file </red>%s<red>.", copyName);
                sender.sendErrorMsg("<red>Check console for details.");
                
                core.plugin().pluginLogger().warn("Encountered IOException while creating file <white>%s</white>", ex, copyName);
                return;
            }
        }
        
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(profilePath)
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions(opt -> opt.serializers(builder -> builder.register(ProfileEntry.class, ProfileSerializer.INSTANCE)))
            .build();
        
        ConfigurationNode node;
        try{
            node = loader.load();
        }catch(IOException ex){
            sender.sendErrorMsg("<red>There was an IOException while loading file </red>%s<red>!", copyName);
            sender.sendErrorMsg("<red>Check console for details.");
            
            core.plugin().pluginLogger().warn("Encountered IOException while loading file <white>%s</white>", ex, copyName);
            return;
        }
        
        try{
            node.node("priority").set(targetProfile.priority());
            node.node("condition").set(targetProfile.condition());
            
            node.node("profiles").setList(ProfileEntry.class, targetProfile.profiles());
            
            node.set(ProfileEntry.class, targetProfile.defaultProfile());
        }catch(SerializationException ex){
            sender.sendErrorMsg("<red>There was a SerializationException while copying values from </red>%s<red>.", profile);
            sender.sendErrorMsg("<red>Check console for details.");
            
            core.plugin().pluginLogger().warn("Encountered IOException while copying data from <white>%s</white> to <white>%s</white>.", ex, profileName, copyName);
            return;
        }
        
        try{
            loader.save(node);
            sender.sendPrefixedMsg("<green>Successfully made copy of </green>%s<green> as </green>%s<green>!", profile, copyName);
            sender.sendPrefixedMsg("<green>Use </green><click:suggest_command:/asl reload>/asl reload</click><green> to load the file.");
        }catch(IOException ex){
            sender.sendErrorMsg("<red>There was an IOException while loading file </red>%s<red>!", copyName);
            sender.sendErrorMsg("<red>Check console for details.");
            
            core.plugin().pluginLogger().warn("Encountered IOException while loading file <white>%s</white>", ex, copyName);
        }
    }
    
    @Command("profiles edit <profile>")
    @CommandDescription("Opens a Dialog to edit a profile.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.edit"})
    @CommandType(CommandType.Type.PLAYER_ONLY)
    public void edit(
        CmdSender sender,
        AdvancedServerList<?> core,
        @NonNull @Argument(description = "Name of the profile to edit.", suggestions = "profiles") String profile
    ){
        if(!core.plugin().isDialogsSupported()){
            sender.sendErrorMsg("This command requires the new Dialog feature to be available!");
            return;
        }
        
        ServerListProfile slp = profile(core, profile);
        if(slp == null){
            sender.sendErrorMsg("No profile with name <white>%s</white> found.", profile);
            return;
        }
        
        DialogLike dialog = core.plugin().createDialog(slp);
        
        sender.audience().showDialog(dialog);
    }
    
    @Suggestions("help")
    public List<String> helpQueries(CommandContext<CmdSender> context){
        return this.commandManager.createHelpHandler()
            .queryRootIndex(context.sender())
            .entries()
            .stream()
            .filter(entry -> context.hasPermission(entry.command().commandPermission()))
            .filter(entry -> {
                CommandType.Type type = entry.command().commandMeta().getOrDefault(COMMAND_TYPE, CommandType.Type.ALL);
                
                return type == CommandType.Type.ALL || type == CommandType.Type.PLAYER_ONLY && context.sender().isPlayer();
            })
            .map(CommandEntry::syntax)
            .toList();
    }
    
    @Suggestions("profiles")
    public List<String> profilesSuggestions(CommandInput input, AdvancedServerList<?> core){
        String name = input.readString();
        List<String> profiles = core.fileHandler().profiles().stream()
            .map(profile -> profile.file().substring(0, profile.file().lastIndexOf('.')))
            .toList();
        if(name.isEmpty())
            return profiles;
        
        List<String> matches = new ArrayList<>(profiles.size());
        for(String str : profiles){
            if(str.length() >= name.length() && str.regionMatches(true, 0, name, 0, name.length()))
                matches.add(str);
        }
        
        return matches;
    }
    
    private String hover(ServerListProfile profile){
        return """
            <grey>Priority: <white>%d</white>
            Condition: <white>%s</white>
            
            Is valid? %s""".formatted(
                profile.priority(),
                profile.condition() == null || profile.condition().isEmpty() ? "<i>None</i>" : profile.condition(), 
                profile.isInvalidProfile() ? "<red>No</red>" : "<green>Yes</green>"
            );
    }
    
    private String profileHover(ProfileEntry entry){
        return """
            <grey>MOTD:
            %s<reset>
            
            <grey>Favicon: %s<reset>
            
            <grey>Hide Playercount? %s
            Hide Playercount Hover? %s
            
            Playercount Hover:
            %s<reset>
            
            <grey>Playercount Text: %s<reset>
            
            <grey>Extra Players: %s<reset>
            <grey>Max Players: %s<reset>
            <grey>Online Players: %s""".formatted(
                entry.motd() == null || entry.motd().isEmpty() ?
                    "<i><white>None</white></i>" : String.join("<reset>\n", entry.motd()),
                entry.favicon() == null || entry.favicon().isEmpty() ?
                    "<i><white>None</white></i>" : entry.favicon(),
                entry.hidePlayersEnabled().getOrDefault(false) ? "<green>Enabled</green>" : "<red>Disabled</red>",
                entry.hidePlayersHoverEnabled().getOrDefault(false) ? "<green>Enabled</green>" : "<red>Disabled</red>",
                entry.players().isEmpty() ? "<i><white>None</white></i>" : String.join("<reset>\n", entry.players()),
                entry.playerCountText() == null || entry.playerCountText().isEmpty() ?
                    "<i><white>None</white></i>" : entry.playerCountText(),
                entry.extraPlayersEnabled().getOrDefault(false) ? entry.extraPlayersCount() : "<red>Disabled</red>",
                entry.maxPlayersEnabled().getOrDefault(false) ? entry.maxPlayersCount() : "<red>Disabled</red>",
                entry.onlinePlayersEnabled().getOrDefault(false) ? entry.onlinePlayersCount() : "<red>Disabled</red>"
        );
    }
    
    public enum Plugin {
        SERVERLISTPLUS,
        MINIMOTD
    }
}
