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

package ch.andre601.advancedserverlist.core.profiles.profile;

import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ProfileManager{
    
    /*
     * Convenience method to get a ProfileEntry instance with values merged.
     * If the random profile's value is not null will it be used instead of the default one.
     */
    public static ProfileEntry merge(ServerListProfile profile){
        ProfileEntry defEntry = profile.defaultProfile();
        ProfileEntry entry = profile.getRandomProfile();
        
        List<String> motd = resolve(defEntry, entry, ProfileEntry::motd);
        List<String> players = resolve(defEntry, entry, ProfileEntry::players);
        String playerCountText = resolve(defEntry, entry, ProfileEntry::playerCountText);
        String favicon = resolve(defEntry, entry, ProfileEntry::favicon);
        NullBool hidePlayersEnabled = resolve(defEntry, entry, ProfileEntry::hidePlayersEnabled);
        NullBool extraPlayersEnabled = resolve(defEntry, entry, ProfileEntry::extraPlayersEnabled);
        NullBool maxPlayersEnabled = resolve(defEntry, entry, ProfileEntry::maxPlayersEnabled);
        NullBool onlinePlayersEnabled = resolve(defEntry, entry, ProfileEntry::onlinePlayersEnabled);
        NullBool hidePlayersHoverEnabled = resolve(defEntry, entry, ProfileEntry::hidePlayersHoverEnabled);
        String extraPlayersCount = resolve(defEntry, entry, ProfileEntry::extraPlayersCount);
        String maxPlayersCount = resolve(defEntry, entry, ProfileEntry::maxPlayersCount);
        String onlinePlayersCount = resolve(defEntry, entry, ProfileEntry::onlinePlayersCount);
        
        return new ProfileEntry.Builder()
            .motd(motd)
            .players(players)
            .playerCountText(playerCountText)
            .favicon(favicon)
            .hidePlayersEnabled(hidePlayersEnabled)
            .extraPlayersEnabled(extraPlayersEnabled)
            .maxPlayersEnabled(maxPlayersEnabled)
            .onlinePlayersEnabled(onlinePlayersEnabled)
            .hidePlayersHoverEnabled(hidePlayersHoverEnabled)
            .extraPlayersCount(extraPlayersCount)
            .maxPlayersCount(maxPlayersCount)
            .onlinePlayersCount(onlinePlayersCount)
            .build();
    }
    
    public static ServerListProfile resolveProfile(AdvancedServerList<?> core, GenericPlayer player, GenericServer server){
        for(ServerListProfile profile : core.fileHandler().profiles()){
            if(profile.isInvalidProfile())
                continue;
            
            if(profile.evalConditions(core, player, server))
                return profile;
        }
        
        return null;
    }
    
    public static ProfileEntry retrieveProfileEntry(ConfigurationNode node){
        List<String> motd = resolveList(node, "motd");
        List<String> players = resolveList(node, "playerCount", "hover");
        String playerCountText = node.node("playerCount", "text").getString("");
        String favicon = node.node("favicon").getString("");
        NullBool hidePlayers = resolveNullBool(node, "playerCount", "hidePlayers");
        NullBool extraPlayersEnabled = resolveNullBool(node, "playerCount", "extraPlayers", "enabled");
        NullBool maxPlayersEnabled = resolveNullBool(node, "playerCount", "maxPlayers", "enabled");
        NullBool onlinePlayersEnabled = resolveNullBool(node, "playerCount", "onlinePlayers", "enabled");
        NullBool hidePlayersHoverEnabled = resolveNullBool(node, "playerCount", "hidePlayersHover");
        String extraPlayers = resolveNullableString(node, "playerCount", "extraPlayers", "amount");
        String maxPlayers = resolveNullableString(node, "playerCount", "maxPlayers", "amount");
        String onlinePlayers = resolveNullableString(node, "playerCount", "onlinePlayers", "amount");
        
        return new ProfileEntry.Builder()
            .motd(motd)
            .players(players)
            .playerCountText(playerCountText)
            .favicon(favicon)
            .hidePlayersEnabled(hidePlayers)
            .extraPlayersEnabled(extraPlayersEnabled)
            .maxPlayersEnabled(maxPlayersEnabled)
            .onlinePlayersEnabled(onlinePlayersEnabled)
            .hidePlayersHoverEnabled(hidePlayersHoverEnabled)
            .extraPlayersCount(extraPlayers)
            .maxPlayersCount(maxPlayers)
            .onlinePlayersCount(onlinePlayers)
            .build();
    }
    
    public static boolean checkOption(Object obj){
        return switch(obj){
            case List<?> list -> !list.isEmpty(); // Check if list isn't empty
            case String str -> !str.isEmpty(); // Check if String is not empty
            case NullBool nb -> nb.getOrDefault(false); // Return NullBool's value
            case null, default -> false;
        };
        
    }
    
    private static <T> T resolve(ProfileEntry defProfile, ProfileEntry profile, Function<ProfileEntry, T> function){
        if(profile == null || !checkOption(function.apply(profile)))
            return function.apply(defProfile);
        
        return function.apply(profile);
    }
    
    private static List<String> resolveList(ConfigurationNode node, Object... path){
        try{
            return node.node(path).getList(String.class);
        }catch(SerializationException ex){
            return Collections.emptyList();
        }
    }
    
    private static NullBool resolveNullBool(ConfigurationNode node, Object... path){
        if(node.node(path).virtual())
            return NullBool.NOT_SET;
        
        return NullBool.resolve(node.node(path).getBoolean());
    }
    
    private static String resolveNullableString(ConfigurationNode node, Object... path){
        if(node.node(path).virtual())
            return null;
        
        return node.node(path).getString();
    }
}
