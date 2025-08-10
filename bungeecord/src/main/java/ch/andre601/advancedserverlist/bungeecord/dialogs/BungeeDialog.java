package ch.andre601.advancedserverlist.bungeecord.dialogs;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.bungeecord.BungeeComponentParser;
import ch.andre601.advancedserverlist.core.interfaces.dialogs.IDialog;
import ch.andre601.advancedserverlist.core.profiles.ServerListProfile;
import net.md_5.bungee.api.dialog.ConfirmationDialog;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.action.ActionButton;
import net.md_5.bungee.api.dialog.action.CustomClickAction;
import net.md_5.bungee.api.dialog.action.RunCommandAction;
import net.md_5.bungee.api.dialog.body.DialogBody;
import net.md_5.bungee.api.dialog.body.PlainMessageBody;
import net.md_5.bungee.api.dialog.input.BooleanInput;
import net.md_5.bungee.api.dialog.input.DialogInput;
import net.md_5.bungee.api.dialog.input.NumberRangeInput;
import net.md_5.bungee.api.dialog.input.TextInput;

import java.util.ArrayList;
import java.util.List;

public class BungeeDialog implements IDialog<Dialog>{
    
    private final String title;
    private final ServerListProfile profile;
    private final int profilesPosition;
    private final List<DialogBody> bodies;
    private final List<DialogInput> inputs;
    
    public BungeeDialog(String title, ServerListProfile profile, int profilesPosition, List<DialogBody> bodies, List<DialogInput> inputs){
        this.title = title;
        this.profile = profile;
        this.profilesPosition = profilesPosition;
        this.bodies = bodies;
        this.inputs = inputs;
    }
    
    @Override
    public Dialog createDialog(){
        return new ConfirmationDialog(
            new DialogBase(BungeeComponentParser.parse(title))
                .body(bodies)
                .inputs(inputs)
                .afterAction(DialogBase.AfterAction.NONE)
                .pause(false)
        ).yes(new ActionButton(
            BungeeComponentParser.parse("<green>Save changes"),
            new CustomClickAction("advancedserverlist:save/" + profile.file() + "/" + profilesPosition)
        ).tooltip(
            BungeeComponentParser.parse("""
                    <grey>Saves your changes made in this Dialog.
                    <grey>Applying changes requires the use of <aqua>/asl reload""")
        )).no(new ActionButton(
            BungeeComponentParser.parse("Back..."),
            new RunCommandAction("/asl profiles edit " + profile.file())
        ).tooltip(
            BungeeComponentParser.parse("""
                    <grey>Returns you to the main Dialog Screen.
                    <grey>Any unsaved changes will be <red>lost</red>!
                    
                    <grey>Click the <green>Save changes</green> button to save your changes.""")
        ));
    }
    
    public static class BungeeDialogBuilder implements IDialogBuilder{
        
        public static BungeeDialogBuilder create(String title, ServerListProfile profile, int profilesPosition){
            return new BungeeDialogBuilder(title, profile, profilesPosition);
        }
        
        private final String title;
        private final ServerListProfile profile;
        private final int profilesPosition;
        
        private final List<DialogBody> bodies = new ArrayList<>();
        private final List<DialogInput> inputs = new ArrayList<>();
        
        public BungeeDialogBuilder(String title, ServerListProfile profile, int profilesPosition){
            this.title = title;
            this.profile = profile;
            this.profilesPosition = profilesPosition;
        }
        
        @Override
        public BungeeDialogBuilder appendBody(String body){
            this.bodies.add(new PlainMessageBody(BungeeComponentParser.parse(body)));
            return this;
        }
        
        @Override
        public BungeeDialogBuilder appendTextInput(String key, String title, String initValue, int maxLength){
            this.inputs.add(new TextInput(key, BungeeComponentParser.parse(title))
                .initial(initValue)
                .maxLength(maxLength)
            );
            return this;
        }
        
        @Override
        public BungeeDialogBuilder appendTextbox(String key, String title, List<String> initValue, int maxLength, int rows){
            this.inputs.add(new TextInput(key, BungeeComponentParser.parse(title))
                .initial(String.join("\n", initValue))
                .maxLength(maxLength)
                .multiline(new TextInput.Multiline(rows, null))
            );
            return this;
        }
        
        @Override
        public BungeeDialogBuilder appendNumberRange(String key, String title, int initValue){
            float minValue = Math.min((float)initValue, -100.0F);
            float maxValue = Math.max((float)initValue, 100.0F);
            
            this.inputs.add(new NumberRangeInput(key, BungeeComponentParser.parse(title), minValue, maxValue)
                .step(1.0F)
                .initial((float)initValue)
            );
            return this;
        }
        
        @Override
        public BungeeDialogBuilder appendBoolean(String key, String title, NullBool initValue){
            this.inputs.add(new BooleanInput(key, BungeeComponentParser.parse(title))
                .initial(initValue.getOrDefault(false))
            );
            return this;
        }
        
        @Override
        public BungeeDialog build(){
            return new BungeeDialog(title, profile, profilesPosition, bodies, inputs);
        }
    }
}
