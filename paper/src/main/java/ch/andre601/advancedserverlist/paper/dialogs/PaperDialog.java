package ch.andre601.advancedserverlist.paper.dialogs;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.api.profiles.ProfileEntry;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
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
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PaperDialog implements IDialog<Dialog>{
    
    private final AdvancedServerList<?> core;
    private final String title;
    private final ServerListProfile profile;
    private final int profilesPosition;
    private final List<DialogBody> bodies;
    private final List<DialogInput> inputs;
    
    private PaperDialog(AdvancedServerList<?> core, String title, ServerListProfile profile, int profilesPosition, List<DialogBody> bodies, List<DialogInput> inputs){
        this.core = core;
        this.title = title;
        this.profile = profile;
        this.profilesPosition = profilesPosition;
        this.bodies = bodies;
        this.inputs = inputs;
    }
    
    @Override
    public Dialog createDialog(){
        return Dialog.create(
            factory -> factory.empty().base(
                DialogBase.builder(ComponentParser.text(title).toComponent())
                    .body(bodies)
                    .inputs(inputs)
                    .afterAction(DialogBase.DialogAfterAction.NONE)
                    .pause(false)
                    .build()
            ).type(DialogType.confirmation(
                ActionButton.builder(ComponentParser.text("<green>Save changes").toComponent())
                    .tooltip(ComponentParser.text("""
                        <grey>Saves your changes made in this Dialog.
                        <grey>Applying changes requires the use of <aqua>/asl reload""").toComponent())
                    .action(
                        DialogAction.customClick(
                            (view, audience) -> handle(audience, view, profile, profilesPosition),
                            ClickCallback.Options.builder().build()
                        )
                    )
                    .build(),
                ActionButton.builder(ComponentParser.text("Back...").toComponent())
                    .tooltip(ComponentParser.text("""
                        <grey>Returns you to the main Dialog Screen.
                        <grey>Any unsaved changes will be <red>lost</red>!
                        
                        <grey>Click the <green>Save changes</green> button to save your changes.""").toComponent())
                    .action(DialogAction.staticAction(ClickEvent.runCommand("/asl profiles edit " + profile.file())))
                    .build()
            ))
        );
    }
    
    private void handle(Audience audience, DialogResponseView view, ServerListProfile slp, int profilesPosition){
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
        
        if(core.fileHandler().saveProfile(slp.file(), (int)newPriority, condition, entries, entry)){
            audience.sendMessage(ComponentParser.textFormatted(
                "<grey>[<aqua>ASL</aqua>] <green>Successfully saved changes for <white>%s</white>!",
                slp.file()
            ).toComponent());
        }else{
            audience.sendMessage(ComponentParser.textFormatted(
                "<grey>[<red>ASL</red>] <red>Error while saving changes for <white>%s</white>! See console for details.",
                slp.file()
            ).toComponent());
        }
    }
    
    public static class PaperDialogBuilder implements IDialogBuilder{
        
        public static PaperDialogBuilder create(AdvancedServerList<?> core, String title, ServerListProfile profile, int profilesPosition){
            return new PaperDialogBuilder(core, title, profile, profilesPosition);
        }
        
        private final AdvancedServerList<?> core;
        private final String title;
        private final ServerListProfile profile;
        private final int profilesPosition;
        private final List<DialogBody> bodies = new ArrayList<>();
        private final List<DialogInput> inputs = new ArrayList<>();
        
        private PaperDialogBuilder(AdvancedServerList<?> core, String title, ServerListProfile profile, int profilesPosition){
            this.core = core;
            this.title = title;
            this.profile = profile;
            this.profilesPosition = profilesPosition;
        }
        
        @Override
        public PaperDialogBuilder appendBody(String body){
            this.bodies.add(DialogBody.plainMessage(ComponentParser.text(body).toComponent()));
            return this;
        }
        
        @Override
        public PaperDialogBuilder appendTextInput(String key, String title, String initValue, int maxLength){
            this.inputs.add(DialogInput.text(key, Component.text(title))
                .initial(initValue == null ? "" : initValue)
                .maxLength(maxLength)
                .build()
            );
            return this;
        }
        
        @Override
        public PaperDialogBuilder appendTextbox(String key, String title, List<String> initValue, int maxLength, int rows){
            this.inputs.add(DialogInput.text(key, Component.text(title))
                .initial(initValue == null ? "" : String.join("\n", initValue))
                .maxLength(maxLength)
                .multiline(TextDialogInput.MultilineOptions.create(rows, null))
                .build()
            );
            return this;
        }
        
        @Override
        public PaperDialogBuilder appendNumberRange(String key, String title, int initValue){
            float minValue = Math.min((float)initValue, -100.0F);
            float maxValue = Math.max((float)initValue, 100.0F);
            
            this.inputs.add(DialogInput.numberRange(key, Component.text(title), minValue, maxValue)
                .step(1.0F)
                .initial((float)initValue)
                .build()
            );
            return this;
        }
        
        @Override
        public PaperDialogBuilder appendBoolean(String key, String title, NullBool initValue){
            this.inputs.add(DialogInput.bool(key, Component.text(title))
                .initial(initValue.getOrDefault(false))
                .build()
            );
            return this;
        }
        
        @Override
        public PaperDialog build(){
            return new PaperDialog(core, title, profile, profilesPosition, bodies, inputs);
        }
    }
}
