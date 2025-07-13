package ch.andre601.advancedserverlist.paper;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PaperDialogBuilder{
    
    private final String title;
    private final String file;
    private final int profilesPosition;
    private final List<DialogBody> bodies = new ArrayList<>();
    private final List<DialogInput> inputs = new ArrayList<>();
    
    public static PaperDialogBuilder create(String title, String file, int profilesPosition){
        return new PaperDialogBuilder(title, file, profilesPosition);
    }
    public PaperDialogBuilder(String title, String file, int profilesPosition1){
        this.title = title;
        this.file = file;
        this.profilesPosition = profilesPosition1;
    }
    
    public PaperDialogBuilder appendInput(String key, String title, String initValue, int maxLength){
        inputs.add(DialogInput.text(key, Component.text(title))
            .initial(initValue == null ? "" : initValue)
            .maxLength(maxLength)
            .build()
        );
        return this;
    }
    
    public PaperDialogBuilder appendTextbox(String key, String title, String initValue, int maxLength, int rows){
        inputs.add(DialogInput.text(key, Component.text(title))
            .initial(initValue == null ? "" : initValue)
            .maxLength(maxLength)
            .multiline(TextDialogInput.MultilineOptions.create(rows, null))
            .build()
        );
        return this;
    }
    
    public PaperDialogBuilder appendRange(String key, String title, int initValue){
        float minRange = Math.min((float)initValue, -100.0f);
        float maxRange = Math.max((float)initValue, 100.0f);
        inputs.add(DialogInput.numberRange(key, Component.text(title), minRange, maxRange)
            .initial((float)initValue)
            .step(1.0f)
            .build()
        );
        return this;
    }
    
    public PaperDialogBuilder appendBoolean(String key, String title, NullBool initValue){
        inputs.add(DialogInput.bool(key, Component.text(title))
            .initial(initValue.getOrDefault(false))
            .build()
        );
        return this;
    }
    
    public PaperDialogBuilder appendBody(String body){
        bodies.add(DialogBody.plainMessage(ComponentParser.text(body).toComponent()));
        return this;
    }
    
    public Dialog build(AdvancedServerList<?> core){
        return Dialog.create(
            factory -> factory.empty()
                .base(
                    DialogBase.builder(ComponentParser.text(title).toComponent())
                        .body(bodies)
                        .inputs(inputs)
                        .build()
                )
                .type(DialogType.confirmation(
                    ActionButton.builder(ComponentParser.text("<green>Save changes").toComponent())
                        .tooltip(ComponentParser.text("""
                            <white>Closes this Dialog while saving your changes.
                            
                            <white>Requires <aqua>/asl reload</aqua> to apply changes.""").toComponent())
                        .action(DialogAction.customClick(
                            (view, audience) -> saveProfile(core, file, view, audience, profilesPosition),
                            ClickCallback.Options.builder().build()
                        ))
                        .build(),
                    ActionButton.builder(ComponentParser.text("<red>Cancel").toComponent())
                        .tooltip(ComponentParser.text("<white>Closes this Dialog without saving your changes.").toComponent())
                        .build()
                ))
        );
    }
    
    private void saveProfile(
        AdvancedServerList<?> core,
        String filename,
        DialogResponseView view,
        Audience audience,
        int profilesPosition
    ){
        ServerListProfile slp = core.fileHandler().profile(filename);
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
        
        if(core.fileHandler().saveProfile(filename, (int)newPriority, condition, entries, entry)){
            audience.sendMessage(ComponentParser.text("<green>Changes saved successfully!").toComponent());
        }else{
            audience.sendMessage(ComponentParser.text("<red>Error while saving changes. Check console for details").toComponent());
        }
    }
}
