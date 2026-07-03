package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.interfaces.commands.CommandType;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialogList;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.profile.ProfileSerializer;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandInput;
import org.jspecify.annotations.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Command("advancedserverlist|asl")
@CommandDescription("Main Command of the plugin.")
@Permission("advancedserverlist.admin")
@CommandType(CommandType.Type.ALL)
public record CmdProfiles(AdvancedServerList<?> core){
    
    public static CmdProfiles create(AdvancedServerList<?> core){
        return new CmdProfiles(core);
    }
    
    @Command("profiles add <name>")
    @CommandDescription("Creates a new Profile with default values applied.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.add"})
    @CommandType(CommandType.Type.ALL)
    public void add(
        CmdSender sender,
        @NonNull @Argument(description = "Name of the profile to create.") String name
    ){
        if(name.isEmpty()){
            sender.sendErrorMsg("<red>Insufficient arguments.");
            sender.sendErrorMsg("<red>A profile name needs to be provided.");
            return;
        }
        
        if(CommandHandler.profile(core(), name) != null){
            sender.sendErrorMsg("<red>A profile with the name </red>%s<red> already exists.", name);
            return;
        }
        
        String file = CommandHandler.profileName(name);
        
        if(core().fileHandler().createFile(file)){
            sender.sendPrefixedMsg("<green>Successfully created </green>%s<green>!", file);
            sender.sendPrefixedMsg("<green>Use <white><click:suggest_command:/asl reload>/asl reload</click></white> to load the file.");
        }else{
            sender.sendErrorMsg("<red>Error while creating new profile.");
            sender.sendErrorMsg("<red>Check the server console for details.");
        }
    }
    
    @Command("profiles copy <profile> <name>")
    public void copy(
        CmdSender sender,
        @NonNull @Argument(description = "Name of the profile to copy.", suggestions = "profiles") String profile,
        @NonNull @Argument(description = "Name of the copy to have.") String name
    ){
        if(profile.isEmpty() || name.isEmpty()){
            sender.sendErrorMsg("<red>Insufficient arguments.");
            sender.sendErrorMsg("<red>A profile name and name for the copy need to be provided.");
            return;
        }
        
        String profileName = CommandHandler.profileName(profile);
        String copyName = CommandHandler.profileName(name);
        
        ServerListProfile original = CommandHandler.profile(core(), profileName);
        
        if(original == null){
            sender.sendErrorMsg("<red>A profile with name </red>%s<red> does not exist.", profile);
            return;
        }
        
        boolean copyExists = CommandHandler.profile(core(), copyName) != null;
        if(copyExists){
            sender.sendErrorMsg("<red>A profile with name </red>%s<red> already exists.", name);
            return;
        }
        
        Path profilePath = core().plugin().folderPath().resolve("profiles").resolve(copyName);
        if(!Files.exists(profilePath)){
            try{
                Files.createFile(profilePath);
            }catch(IOException ex){
                sender.sendErrorMsg("<red>Encountered an IOException while creating </red>%s<red>.", copyName);
                sender.sendErrorMsg("<red>Please see the server console for further details.");
                
                core.plugin().pluginLogger().warn("Encountered IOException while creating file <white>%s</white>", ex, copyName);
                return;
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
                sender.sendErrorMsg("<red>Encountered an IOException while loading file </red>%s<red>.", copyName);
                sender.sendErrorMsg("<red>Please see the server console for further details.");
                
                core().plugin().pluginLogger().warn("Encountered IOException while loading file <white>%s</white>.", ex, copyName);
                return;
            }
            
            try{
                node.node("priority").set(original.priority());
                node.node("condition").set(original.condition());
                
                node.node("profiles").setList(ProfileEntry.class, original.profiles());
                
                node.set(ProfileEntry.class, original.defaultProfile());
            }catch(SerializationException ex){
                sender.sendErrorMsg("<red>Encountered a SerializationException while copying values from </red>%s<red> to </red>%s<red>.", profileName, copyName);
                sender.sendErrorMsg("<red>Please see the server console for further details.");
                
                core().plugin().pluginLogger().warn("<red>Encountered a SerializationException while copying values from </red>%s<red> to </red>%s<red>.", ex, profileName, copyName);
                return;
            }
            
            try{
                loader.save(node);
                sender.sendPrefixedMsg("<green>Successfully created copy of </green>%s<green> as </green>%s<green>!", profileName, copyName);
                sender.sendPrefixedMsg("<green>Use <white><click:suggest_command:/asl reload>/asl reload</click></white> to load the file.");
            }catch(IOException ex){
                sender.sendErrorMsg("<red>There was an IOException while loading file </red>%s<red>!", copyName);
                sender.sendErrorMsg("<red>Please see the server console for further details.");
                
                core.plugin().pluginLogger().warn("Encountered an IOException while loading file <white>%s</white>", ex, copyName);
            }
        }
    }
    
    @Command("profiles edit <profile>")
    @CommandDescription("Opens a Dialog to edit a specific profile ingame.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.edit"})
    @CommandType(CommandType.Type.PLAYER_ONLY)
    public void edit(
        CmdSender sender,
        @NonNull @Argument(description = "Name of the profile to edit.", suggestions = "profiles") String profile
    ){
        if(!core().plugin().isDialogsSupported()){
            sender.sendErrorMsg("Dialogs are not supported on this server or proxy version.");
            return;
        }
        
        ServerListProfile slp = CommandHandler.profile(core(), profile);
        if(slp == null){
            sender.sendErrorMsg("<red>A profile with name </red>%s<red> does not exist.", profile);
            return;
        }
        
        IDialogList.IDialogListBuilder listBuilder = core().plugin().createDialogListBuilder(slp.file());
        String warning = """
            <gold><bold>WARNING!</bold></gold>
            
            Dialog-based Profile editing is currently an <italic>experimental feature</italic>!
            Please create backups of your profiles before using this feature and report any bugs and/or glitches you encounter.""";
        
        listBuilder.appendBody(warning);
        
        listBuilder.appendDialog(
            createDialog("Main Settings", warning, slp, slp.defaultProfile(), slp.priority(), slp.condition(), -1)
        );
        
        for(int i = 0; i < slp.profiles().size(); i++){
            listBuilder.appendDialog(
                createDialog("Profiles #" + (i + 1), warning, slp, slp.profiles().get(i), -1, null, i)
            );
        }
        
        listBuilder.build().show(sender);
    }
    
    @Command("profiles info <profile>")
    @CommandDescription("Provides info about a specific profile.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.info"})
    @CommandType(CommandType.Type.PLAYER_ONLY)
    public void info(
        CmdSender sender,
        @NonNull @Argument(description = "Name of the profile to view.", suggestions = "profiles") String profile
    ){
        if(profile.isEmpty()){
            sender.sendErrorMsg("<red>Insufficient arguments.");
            sender.sendErrorMsg("<red>A profile name needs to be provided.");
            return;
        }
        
        ServerListProfile slp = CommandHandler.profile(core(), profile);
        
        if(slp == null){
            sender.sendErrorMsg("<red>Unable to find profile with name </red>%s<red>.", profile);
            sender.sendErrorMsg("<red>Make sure you typed it correctly.");
            return;
        }
        
        ProfileOptionsSender optionsSender = new ProfileOptionsSender(slp.file());
        
        optionsSender
            .append(
                ProfileOptionsSender.OptionStringBuilder.of("Priority", slp.priority())
                    .option(ProfileOption.PRIORITY)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Condition", slp.condition())
                    .option(ProfileOption.CONDITION)
            ).append(
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
                    .option(ProfileOption.MOTD)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Favicon", entry.favicon())
                    .option(ProfileOption.FAVICON)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Player count")
                    .prefix("└─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Hide Player count?", entry.hidePlayersEnabled())
                    .prefix("     ├─")
                    .option(ProfileOption.PLAYERCOUNT_HIDEPLAYERS)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Hide Player count hover?", entry.hidePlayersHoverEnabled())
                    .prefix("     ├─")
                    .option(ProfileOption.PLAYERCOUNT_HIDEPLAYERSHOVER)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Player count hover", entry.players())
                    .prefix("     ├─")
                    .option(ProfileOption.PLAYERCOUNT_HOVER)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Player count text", entry.playerCountText())
                    .prefix("     ├─")
                    .option(ProfileOption.PLAYERCOUNT_TEXT)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Extra Players")
                    .prefix("     ├─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Enabled?", entry.extraPlayersEnabled())
                    .prefix("     │    ├─")
                    .option(ProfileOption.PLAYERCOUNT_EXTRAPLAYERS_ENABLED)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Amount", entry.extraPlayersCount())
                    .prefix("     │    └─")
                    .option(ProfileOption.PLAYERCOUNT_EXTRAPLAYERS_AMOUNT)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Max Players")
                    .prefix("     ├─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Enabled?", entry.maxPlayersEnabled())
                    .prefix("     │    ├─")
                    .option(ProfileOption.PLAYERCOUNT_MAXPLAYERS_ENABLED)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Amount", entry.maxPlayersCount())
                    .prefix("     │    └─")
                    .option(ProfileOption.PLAYERCOUNT_MAXPLAYERS_AMOUNT)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Online Players")
                    .prefix("     └─")
                    .color(ProfileOptionsSender.ColorName.AQUA)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Enabled?", entry.onlinePlayersEnabled())
                    .prefix("          ├─")
                    .option(ProfileOption.PLAYERCOUNT_ONLINEPLAYERS_ENABLED)
            ).append(
                ProfileOptionsSender.OptionStringBuilder.of("Amount", entry.onlinePlayersCount())
                    .prefix("          └─")
                    .option(ProfileOption.PLAYERCOUNT_ONLINEPLAYERS_AMOUNT)
            ).send(sender);
    }
    
    @Command("profiles list")
    @CommandDescription("Lists all available profiles with priority, conditions and if they are valid.")
    @Permission({"advancedserverlist.admin", "advancedserverlist.command.profiles", "advancedserverlist.command.profiles.list"})
    @CommandType(CommandType.Type.ALL)
    public void list(CmdSender sender){
        sender.sendMsg();
        sender.sendPrefixedMsg("Available Profiles:");
        sender.sendMsg("<dark_grey>|");
        
        List<ServerListProfile> profiles = core().fileHandler().profiles().stream()
            .sorted(Comparator.comparingInt(ServerListProfile::priority).reversed())
            .toList();
        
        if(profiles.isEmpty()){
            sender.sendMsg("<dark_grey></dark_grey> <i>No profiles found.</i>");
        }else{
            for(int i = 0; i < profiles.size(); i++){
                sender.sendMsg(
                    "<dark_grey>%s</dark_grey> <aqua>" +
                    "<hover:show_text:\"%s\">" +
                    "<click:suggest_command:/asl profiles info %s>" +
                    "%s" +
                    "</click>" +
                    "</hover>" +
                    "</aqua>",
                    (i + 1) == profiles.size() ? "" : "-",
                    hover(profiles.get(i)),
                    profiles.get(i).file(),
                    profiles.get(i).file()
                );
            }
        }
        
        sender.sendMsg();
        sender.sendMsg("<grey><i>Hover</i></grey> = Show details.");
        sender.sendMsg("<grey><i>Click</i></grey> = Get Command.");
    }
    
    @Suggestions("profiles")
    public List<String> profilesSuggestions(CommandInput input){
        String name = input.readString();
        List<String> profiles = core().fileHandler().profiles().stream()
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
            
            Valid? %s""".formatted(
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
            
            <grey>Hide Player count? %s
            Hide Player count Hover? %s
            
            Player count Hover:
            %s<reset>
            
            <grey>Player count Text: %s<reset>
            
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
    
    private IDialog<?> createDialog(
        String title,
        String warning,
        ServerListProfile slp,
        ProfileEntry entry,
        int priority,
        String condition,
        int index
    ){
        IDialog.IDialogBuilder builder = core.plugin().createDialogBuilder(title, slp, index);
        builder.appendBody(warning);
        
        if(index < 0){
            builder.appendNumberRange(ProfileOption.PRIORITY, "Priority", priority)
                .appendTextInput(ProfileOption.CONDITION, "Condition", condition, 100);
        }
        
        builder.appendTextbox(ProfileOption.MOTD, "MOTD", entry.motd(), 200, 2)
            .appendTextInput(ProfileOption.FAVICON, "Favicon", entry.favicon(), 100)
            .appendBoolean(ProfileOption.PLAYERCOUNT_HIDEPLAYERS, "Hide Player Count", entry.hidePlayersEnabled())
            .appendBoolean(ProfileOption.PLAYERCOUNT_HIDEPLAYERSHOVER, "Hide Player Count Hover", entry.hidePlayersHoverEnabled())
            .appendTextbox(ProfileOption.PLAYERCOUNT_HOVER, "Player Count Hover", entry.players(), 200, 10)
            .appendTextInput(ProfileOption.PLAYERCOUNT_TEXT, "Player Count Text", entry.playerCountText(), 100)
            .appendBoolean(ProfileOption.PLAYERCOUNT_EXTRAPLAYERS_ENABLED, "Enable Extra Players", entry.extraPlayersEnabled())
            .appendTextInput(ProfileOption.PLAYERCOUNT_EXTRAPLAYERS_AMOUNT, "Extra Players Count", entry.extraPlayersCount(), 100)
            .appendBoolean(ProfileOption.PLAYERCOUNT_MAXPLAYERS_ENABLED, "Enable Max Players", entry.maxPlayersEnabled())
            .appendTextInput(ProfileOption.PLAYERCOUNT_MAXPLAYERS_AMOUNT, "Max Players Count", entry.maxPlayersCount(), 100)
            .appendBoolean(ProfileOption.PLAYERCOUNT_ONLINEPLAYERS_ENABLED, "Enable Online Players", entry.onlinePlayersEnabled())
            .appendTextInput(ProfileOption.PLAYERCOUNT_ONLINEPLAYERS_AMOUNT, "Online Players Count", entry.onlinePlayersCount(), 100);
        
        return builder.build();
    }
    
    public enum ProfileOption{
        PRIORITY,
        CONDITION,
        MOTD,
        FAVICON,
        PLAYERCOUNT_HIDEPLAYERS,
        PLAYERCOUNT_HIDEPLAYERSHOVER,
        PLAYERCOUNT_HOVER,
        PLAYERCOUNT_TEXT,
        PLAYERCOUNT_EXTRAPLAYERS_ENABLED,
        PLAYERCOUNT_EXTRAPLAYERS_AMOUNT,
        PLAYERCOUNT_MAXPLAYERS_ENABLED,
        PLAYERCOUNT_MAXPLAYERS_AMOUNT,
        PLAYERCOUNT_ONLINEPLAYERS_ENABLED,
        PLAYERCOUNT_ONLINEPLAYERS_AMOUNT;
        
        public static List<String> list(){
            return Arrays.stream(values())
                .map(ProfileOption::toString)
                .toList();
        }
        
        public static ProfileOption fromString(String input){
            for(ProfileOption option : values()){
                if(option.toString().equalsIgnoreCase(input))
                    return option;
            }
            
            return null;
        }
        
        @Override
        public String toString(){
            return this.name().toLowerCase(Locale.ROOT).replace("_", ".");
        }
        
        public String toDialogKey(){
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
