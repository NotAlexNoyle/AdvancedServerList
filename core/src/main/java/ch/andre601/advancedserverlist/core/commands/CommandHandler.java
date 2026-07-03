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
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.key.CloudKey;

import java.util.*;

public class CommandHandler{
    
    public static final CloudKey<CommandType.Type> COMMAND_TYPE = CloudKey.of("command_type", CommandType.Type.class);
    public static final CloudKey<String> OPTION = CloudKey.of("profile_option", String.class);
    
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
        
        switch(CmdProfiles.ProfileOption.fromString(option)){
            case PRIORITY -> {
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
            case CONDITION -> {
                oldValues.add(slp.condition());
                
                if(value == null || value.isEmpty()){
                    condition = "";
                    break;
                }
                
                condition = value;
                newValues.add(value);
            }
            case MOTD -> {
                oldValues.addAll(entry.motd());
                
                if(value == null || value.isEmpty()){
                    builder.motd(Collections.emptyList());
                    break;
                }
                
                String[] lines = value.split("\\\\n|<newline>|<br>");
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
            case FAVICON -> {
                oldValues.add(entry.favicon());
                
                if(value == null || value.isEmpty()){
                    builder.favicon("");
                    break;
                }
                
                builder.favicon(value);
                
                newValues.add(value);
            }
            case PLAYERCOUNT_HIDEPLAYERS -> {
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
            case PLAYERCOUNT_HIDEPLAYERSHOVER -> {
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
            case PLAYERCOUNT_HOVER -> {
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
            case PLAYERCOUNT_TEXT -> {
                if(!entry.playerCountText().isEmpty())
                    oldValues.add(entry.playerCountText());
                
                if(value == null || value.isEmpty()){
                    builder.playerCountText("");
                    break;
                }
                
                builder.playerCountText(value);
                
                newValues.add(value);
            }
            case PLAYERCOUNT_EXTRAPLAYERS_ENABLED -> {
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
            case PLAYERCOUNT_EXTRAPLAYERS_AMOUNT -> {
                if(!entry.extraPlayersCount().isEmpty())
                    oldValues.add(entry.extraPlayersCount());
                
                if(value == null || value.isEmpty()){
                    builder.extraPlayersCount("");
                    break;
                }
                
                builder.extraPlayersCount(value);
                
                newValues.add(value);
            }
            case PLAYERCOUNT_MAXPLAYERS_ENABLED -> {
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
            case PLAYERCOUNT_MAXPLAYERS_AMOUNT -> {
                if(!entry.maxPlayersCount().isEmpty())
                    oldValues.add(entry.extraPlayersCount());
                
                if(value == null || value.isEmpty()){
                    builder.maxPlayersCount("");
                    break;
                }
                
                builder.maxPlayersCount(value);
                
                newValues.add(value);
            }
            case PLAYERCOUNT_ONLINEPLAYERS_ENABLED -> {
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
            case PLAYERCOUNT_ONLINEPLAYERS_AMOUNT -> {
                if(!entry.onlinePlayersCount().isEmpty())
                    oldValues.add(entry.onlinePlayersCount());
                
                if(value == null || value.isEmpty()){
                    builder.onlinePlayersCount("");
                    break;
                }
                
                builder.onlinePlayersCount(value);
                
                newValues.add(value);
            }
            case null, default -> {
                sender.sendErrorMsg("<red>Unknown option </red>%s<red>!", option);
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
    
    public static ServerListProfile profile(AdvancedServerList<?> core, String name){
        return core.fileHandler().profiles().stream()
            .filter(p -> p.file().equalsIgnoreCase(profileName(name)))
            .findFirst()
            .orElse(null);
    }
    
    public static String profileName(String name){
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
}
