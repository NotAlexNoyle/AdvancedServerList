package ch.andre601.advancedserverlist.paper.listeners;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import ch.andre601.advancedserverlist.paper.PaperCore;
import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Locale;

public class DialogListener implements Listener{
    
    private final PaperCore plugin;
    
    public DialogListener(PaperCore plugin){
        this.plugin = plugin;
    }
    
    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onPlayerCustomClick(PlayerCustomClickEvent event){
        if(!event.getIdentifier().namespace().equalsIgnoreCase("advancedserverlist"))
            return;
        
        if(!event.getIdentifier().value().toLowerCase(Locale.ROOT).startsWith("profiles/save/"))
            return;
        
        if(!(event.getCommonConnection() instanceof PlayerGameConnection connection))
            return;
        
        Player player = connection.getPlayer();
        String file = event.getIdentifier().value().substring("profiles/save/".length());
        ServerListProfile slp = plugin.core().fileHandler().profile(file);
        
        if(slp == null){
            player.sendMessage(ComponentParser.textFormatted(
                "<grey>[<red>ASL</red>] <red>Unable to save changes. Couldn't find file <white>%s</white>.",
                file
            ).toComponent());
            return;
        }
        
        BinaryTagHolder tag = event.getTag();
        if(tag == null){
            player.sendMessage(ComponentParser.text(
                "<grey>[<red>ASL</red>] <red>Unable to save changes. Received BinaryTagHolder was null."
            ).toComponent());
            return;
        }
        
        int profilesPosition;
        try{
            profilesPosition = Integer.parseInt(tag.string());
        }catch(NumberFormatException ignored){
            player.sendMessage(ComponentParser.textFormatted(
                "<grey>[<red>ASL</red>] <red>Unable to save changes. Received BinaryTagHolder <white>%s</white> was not a number.",
                tag.string()
            ).toComponent());
            return;
        }
        
        DialogResponseView view = event.getDialogResponseView();
        if(view == null){
            player.sendMessage(ComponentParser.text(
                "<grey>[<red>ASL</red>] <red>Unable to save changes. Received DialogResponseView was null."
            ).toComponent());
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
        
        if(plugin.core().fileHandler().saveProfile(file, (int)newPriority, condition, entries, entry)){
            player.sendMessage(ComponentParser.textFormatted(
                "<grey>[<aqua>ASL</aqua>] <green>Successfully saved changes for file <white>%s</white>!",
                file
            ).toComponent());
        }else{
            player.sendMessage(ComponentParser.textFormatted(
                "<grey>[<red>ASL</red>] <red>Error while saving changes for file <white>%s</white>. " +
                "Check console for details.",
                file
            ).toComponent());
        }
    }
}
