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

package ch.andre601.advancedserverlist.core.events;

import ch.andre601.advancedserverlist.api.events.GenericServerListEvent;
import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.interfaces.core.PluginCore;
import ch.andre601.advancedserverlist.core.interfaces.events.GenericEventWrapper;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.core.profiles.profile.ProfileManager;
import ch.andre601.advancedserverlist.core.profiles.replacer.StringReplacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.List;

public class PingEventHandler{
    
    public static <F, P extends GenericPlayer> ProfileEntry handleEvent(GenericEventWrapper<F, P> event){
        event.plugin().pluginLogger().debug(PingEventHandler.class, "Received ping event. Handling it...");
        if(event.isInvalidProtocol()){
            event.plugin().pluginLogger().debug(PingEventHandler.class, "Not handling event. Protocol was invalid.");
            return null;
        }
        
        PluginCore<F> plugin = event.plugin();
        String host = event.virtualHost();
        PluginLogger logger = plugin.pluginLogger();
        
        logger.debug(PingEventHandler.class, "Protocol valid. Continue handling...");
        
        int online = event.onlinePlayers();
        int max = event.maxPlayers();
        
        P player = event.createPlayer(
            plugin.core().playerHandler().getCachedPlayer(event.playerIP()),
            event.protocolVersion()
        );
        GenericServer server = event.createServer(online, max, host);
        
        ServerListProfile profile = ProfileManager.resolveProfile(plugin.core(), player, server);
        
        if(profile == null){
            logger.debugWarn(PingEventHandler.class, "Server List Profile couldn't be resolved properly. Cancelling event handling...");
            return null;
        }
        
        logger.debug(PingEventHandler.class, "Received valid Server List Profile. Calling PreServerListSetEvent...");
        
        GenericServerListEvent slEvent = event.callEvent(ProfileManager.merge(profile));
        if(slEvent == null || slEvent.isCancelled()){
            logger.debug(PingEventHandler.class, "PreServerListSetEvent was cancelled. Stopping ping handling...");
            return null;
        }
        
        logger.debug(PingEventHandler.class, "PreServerListSetEvent completed. Proceeding with ping handling...");
        
        ProfileEntry entry = slEvent.getEntry();
        if(entry.isInvalid()){
            logger.debugWarn(PingEventHandler.class, "No valid ProfileEntry retrieved. Cancelling ping handling...");
            return null;
        }
        
        if(ProfileManager.checkOption(entry.onlinePlayersEnabled()) && ignoreMaintenance(event, "onlinePlayers")){
            logger.debug(PingEventHandler.class, "'playerCount -> onlinePlayers -> enabled' option is true. Trying to apply '%s'...", entry.onlinePlayersCount());
            String onlinePlayers = ComponentParser.text(entry.onlinePlayersCount())
                .modifyText(text -> StringReplacer.replace(text, player, server))
                .modifyText(text -> event.parsePAPIPlaceholders(text, player))
                .applyMMTags(event.plugin())
                .toString();
            
            try{
                online = onlinePlayers == null ? online : Integer.parseInt(onlinePlayers);
                event.onlinePlayers(online);
            }catch(NumberFormatException ex){
                logger.warn("Option 'playerCount -> onlinePlayers -> amount' is not a valid Number!");
            }
        }
        
        boolean extraPlayersEnabled = ProfileManager.checkOption(entry.extraPlayersEnabled());
        
        if(extraPlayersEnabled && ignoreMaintenance(event, "extraPlayers")){
            logger.debug(PingEventHandler.class, "'playerCount -> extraPlayers -> enabled' option is true. Trying to apply '%s'...", entry.extraPlayersCount());
            String extraPlayers = ComponentParser.text(entry.extraPlayersCount())
                .modifyText(text -> StringReplacer.replace(text, player, server))
                .modifyText(text -> event.parsePAPIPlaceholders(text, player))
                .applyMMTags(event.plugin())
                .toString();
            
            try{
                max = online + (extraPlayers == null ? 0 : Integer.parseInt(extraPlayers));
                
                logger.debug(PingEventHandler.class, "'playerCount -> extraPlayers -> amount' is a valid number. Applying it...");
                event.maxPlayers(max);
            }catch(NumberFormatException ex){
                logger.warn("Option 'playerCount -> extraPlayers -> amount' is not a valid Number!");
            }
        }
        
        if(ProfileManager.checkOption(entry.maxPlayersEnabled()) && !extraPlayersEnabled && ignoreMaintenance(event, "maxPlayers")){
            logger.debug(PingEventHandler.class, "'playerCount -> maxPlayers -> enabled' option is true. Trying to apply '%s'...", entry.maxPlayersCount());
            String maxPlayers = ComponentParser.text(entry.maxPlayersCount())
                .modifyText(text -> StringReplacer.replace(text, player, server))
                .modifyText(text -> event.parsePAPIPlaceholders(text, player))
                .applyMMTags(event.plugin())
                .toString();
            
            try{
                max = maxPlayers == null ? 0 : Integer.parseInt(maxPlayers);
                
                logger.debug(PingEventHandler.class, "'playerCount -> maxPlayers -> amount' is a valid number. Applying it...");
                event.maxPlayers(max);
            }catch(NumberFormatException ex){
                logger.warn("Option 'playerCount -> maxPlayers -> amount' is not a valid Number!");
            }
        }
        
        GenericServer finalServer = event.createServer(online, max, host);
        
        if(ProfileManager.checkOption(entry.motd()) && ignoreMaintenance(event, "motd")){
            logger.debug(PingEventHandler.class, "'motd' option present. Applying ['%s']...", String.join("', '", entry.motd()));
            
            List<Component> motd = entry.motd().stream()
                .map(line -> ComponentParser.text(line)
                    .modifyText(text -> StringReplacer.replace(text, player, finalServer))
                    .modifyText(text -> event.parsePAPIPlaceholders(text, player))
                    .modifyText(text -> plugin.core().textCenterUtil().getCenteredText(text))
                    .applyMMTags(event.plugin())
                    .toComponent()
                )
                .toList();
            
            event.motd(Component.join(JoinConfiguration.separator(Component.newline()), motd));
        }
        
        boolean hidePlayers = ProfileManager.checkOption(entry.hidePlayersEnabled());
        
        if(hidePlayers && ignoreMaintenance(event, "hidePlayers") && ignoreMaintenance(event, "hidePlayersHover")){
            logger.debug(PingEventHandler.class, "'playerCount -> hidePlayers' enabled. Hiding player count...");
            
            event.hidePlayers();
        }
        
        if(ProfileManager.checkOption(entry.playerCountText()) && !hidePlayers && ignoreMaintenance(event, "playerCountText")){
            logger.debug(PingEventHandler.class, "'playerCount -> text' option set. Applying '%s'...", entry.playerCountText());
            
            event.playerCount(
                ComponentParser.text(entry.playerCountText())
                    .modifyText(text -> StringReplacer.replace(text, player, finalServer))
                    .modifyText(text -> event.parsePAPIPlaceholders(text, player))
                    .applyMMTags(event.plugin())
                    .toString()
            );
        }
        
        boolean hidePlayersHover = ProfileManager.checkOption(entry.hidePlayersHoverEnabled());
        if(hidePlayersHover){
            logger.debug(PingEventHandler.class, "'playerCount -> hidePlayersHover' option set. Hiding Player Hover...");
            event.playersHidden();
        }
        
        if(ProfileManager.checkOption(entry.players()) && !hidePlayers && !hidePlayersHover && ignoreMaintenance(event, "playerCountHover")){
            logger.debug(PingEventHandler.class, "'playerCount -> hover' option set. Applying ['%s']...", String.join("', '", entry.players()));
            
            event.players(entry.players(), player, server);
        }
        
        if(ProfileManager.checkOption(entry.favicon()) && ignoreMaintenance(event, "favicon")){
            logger.debug(PingEventHandler.class, "'favicon' option set. Resolving '%s'...", entry.favicon());
            
            String faviconString = StringReplacer.modifier(entry.favicon())
                .modify(text -> StringReplacer.replace(text, player, server))
                .modify(text -> event.parsePAPIPlaceholders(text, player))
                .asString();
            
            F favicon = plugin.faviconHandler().favicon(faviconString);
            
            if(favicon == null){
                logger.debugWarn(PingEventHandler.class, "Favicon was invalid or not yet resolved! Using default favicon of Server/Proxy...");
                
                event.defaultFavicon();
            }else{
                logger.debug(PingEventHandler.class, "Applying favicon...");
                
                event.favicon(favicon);
            }
        }
        
        logger.debug(PingEventHandler.class, "Event handling completed. Updating Ping data...");
        event.updateEvent();
        
        return entry;
    }
    
    private static <F, P extends GenericPlayer> boolean ignoreMaintenance(GenericEventWrapper<F, P> event, String option){
        if(!event.isMaintenanceModeActive())
            return true;
        
        return !event.plugin().core().fileHandler().getBool(true, "disableDuringMaintenance", option);
    }
}
