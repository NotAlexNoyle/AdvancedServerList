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

package ch.andre601.advancedserverlist.paper.objects.placeholders;

import ch.andre601.advancedserverlist.api.bukkit.objects.BukkitPlayer;
import ch.andre601.advancedserverlist.api.bukkit.objects.BukkitServer;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.profile.ProfileManager;
import ch.andre601.advancedserverlist.core.profiles.replacer.StringReplacer;
import ch.andre601.advancedserverlist.paper.PaperCore;
import ch.andre601.advancedserverlist.paper.objects.impl.PaperPlayerImpl;
import ch.andre601.advancedserverlist.paper.objects.impl.PaperServerImpl;
import com.viaversion.viaversion.api.Via;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PAPIPlaceholders extends PlaceholderExpansion{
    
    private final PaperCore plugin;
    
    public PAPIPlaceholders(PaperCore plugin){
        this.plugin = plugin;
        this.register();
    }
    
    @Override
    public @NonNull String getIdentifier(){
        return "asl";
    }
    
    @Override
    public @NonNull String getAuthor(){
        return "Andre601";
    }
    
    @Override
    public @NonNull String getVersion(){
        return plugin.core().version();
    }
    
    @Override
    public boolean persist(){
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player pl, @NonNull String identifier){
        String host = pl.getVirtualHost() == null ? null : pl.getVirtualHost().getHostString();
        CachedPlayer cached = plugin.core().playerHandler().getCachedPlayer(pl.getUniqueId());
        
        int protocol = resolveProtocol(pl);
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        
        BukkitServer server = new PaperServerImpl(plugin.worldCache().worlds(), online, max, host);
        BukkitPlayer player = new PaperPlayerImpl(pl, cached, protocol);
        
        ServerListProfile profile = ProfileManager.resolveProfile(plugin.core(), player, server);
        if(profile == null)
            return null;
        
        ProfileEntry entry = ProfileManager.merge(profile);
        
        Integer onlinePlayers = null;
        if(ProfileManager.checkOption(entry.onlinePlayersEnabled())){
            onlinePlayers = parseNumberOption(entry.onlinePlayersCount(), pl, player, server);
            online = onlinePlayers == null ? online : onlinePlayers;
        }
        
        Integer maxPlayers = null;
        if(ProfileManager.checkOption(entry.maxPlayersEnabled())){
            maxPlayers = parseNumberOption(entry.maxPlayersCount(), pl, player, server);
            max = maxPlayers == null ? 0 : maxPlayers;
        }
        
        Integer extraPlayers = null;
        if(ProfileManager.checkOption(entry.extraPlayersEnabled())){
            extraPlayers = parseNumberOption(entry.extraPlayersCount(), pl, player, server);
            max = online + (extraPlayers == null ? 0 : extraPlayers);
        }
        
        BukkitServer finalServer = new PaperServerImpl(plugin.worldCache().worlds(), online, max, host);
        return switch(identifier.toLowerCase(Locale.ROOT)){
            case "favicon" -> getOption(entry.favicon(), pl, player, finalServer);
            case "motd" -> getOption(entry.motd(), pl, player, finalServer, true);
            case "playercount_extraplayers" -> extraPlayers == null ? "" : String.valueOf(extraPlayers);
            case "playercount_hideplayers" -> String.valueOf(entry.hidePlayersEnabled().getOrDefault(false));
            case "playercount_hideplayershover" -> String.valueOf(entry.hidePlayersHoverEnabled().getOrDefault(false));
            case "playercount_hover" -> getOption(entry.players(), pl, player, finalServer, false);
            case "playercount_maxplayers" -> maxPlayers == null ? "" : String.valueOf(maxPlayers);
            case "playercount_onlineplayers" -> onlinePlayers == null ? "" : String.valueOf(onlinePlayers);
            case "playercount_text" -> getOption(entry.playerCountText(), pl, player, finalServer);
            case "server_playersmax" -> String.valueOf(max);
            default -> null;
        };
    }
    
    private int resolveProtocol(Player player){
        if(Bukkit.getPluginManager().isPluginEnabled("ViaVersion"))
            return Via.getAPI().getPlayerVersion(player.getUniqueId());
        
        // Since ViaVersion isn't installed is the player's protocol version the same as the server.
        //noinspection deprecation
        return Bukkit.getUnsafe().getProtocolVersion();
    }
    
    private String getOption(String str, Player pl, BukkitPlayer player, BukkitServer server){
        if(str == null)
            return "";
        
        return getOption(Collections.singletonList(str), pl, player, server, false);
    }
    
    private String getOption(List<String> list, Player pl, BukkitPlayer player, BukkitServer server, boolean isMotd){
        if(list.isEmpty())
            return "";
        
        return list.stream()
            .map(line -> ComponentParser.text(line)
                .modifyText(text -> StringReplacer.replace(text, player, server))
                .modifyText(text -> PlaceholderAPI.setPlaceholders(pl, text))
                .modifyText(text -> {
                    if(isMotd)
                        return plugin.core().textCenterUtil().getCenteredText(text);
                    
                    return text;
                })
                .toString()
            )
            .collect(Collectors.joining("\n"));
    }
    
    private Integer parseNumberOption(String option, Player pl, BukkitPlayer player, BukkitServer server){
        String parsed = ComponentParser.text(option)
            .modifyText(text -> StringReplacer.replace(text, player, server))
            .modifyText(text -> PlaceholderAPI.setPlaceholders(pl, text))
            .toString();
        
        try{
            return Integer.parseInt(parsed);
        }catch(NumberFormatException ex){
            return null;
        }
    }
}
