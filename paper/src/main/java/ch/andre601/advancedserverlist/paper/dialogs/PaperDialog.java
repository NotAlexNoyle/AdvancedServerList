package ch.andre601.advancedserverlist.paper.dialogs;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.core.dialogs.AbstractDialog;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PaperDialog extends AbstractDialog{
    
    private final List<DialogBody> bodies;
    private final List<DialogInput> inputs;
    
    private PaperDialog(String title, ServerListProfile profile, int profilesPosition, List<DialogBody> bodies, List<DialogInput> inputs){
        super(title, profile, profilesPosition);
        
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
                            Key.key("advancedserverlist:profiles/save/" + profile.file()),
                            BinaryTagHolder.binaryTagHolder(String.valueOf(profilesPosition))
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
    
    public static class PaperDialogBuilder extends AbstractDialogBuilder{
        
        public static PaperDialogBuilder create(String title, ServerListProfile profile, int profilesPosition){
            return new PaperDialogBuilder(title, profile, profilesPosition);
        }
        
        private final List<DialogBody> bodies = new ArrayList<>();
        private final List<DialogInput> inputs = new ArrayList<>();
        
        private PaperDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
            super(title, profile, profilesPosition);
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
                .multiline(TextDialogInput.MultilineOptions.create(null, rows))
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
            return new PaperDialog(title, profile, profilesPosition, bodies, inputs);
        }
    }
}
