package ch.andre601.advancedserverlist.paper.dialogs;

import ch.andre601.advancedserverlist.core.dialogs.AbstractDialog;
import ch.andre601.advancedserverlist.core.dialogs.AbstractDialogList;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;
import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

public class PaperDialogList extends AbstractDialogList{
    
    protected PaperDialogList(String title, List<String> bodies, List<AbstractDialog> dialogs){
        super(title, bodies, dialogs);
    }
    
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void show(CmdSender sender){
        List<DialogBody> dialogBodies = bodies.stream()
            .<DialogBody>map(text -> DialogBody.plainMessage(ComponentParser.text(text).toComponent()))
            .toList();
        List<Dialog> paperDialogs = dialogs.stream()
            .filter(dialog -> dialog instanceof PaperDialog)
            .map(dialog -> (PaperDialog)dialog)
            .map(PaperDialog::createDialog)
            .toList();
        
        DialogBase.Builder builder = DialogBase.builder(Component.text(title))
            .body(dialogBodies);
        
        RegistrySet<Dialog> dialogRegistry = RegistrySet.valueSet(RegistryKey.DIALOG, paperDialogs);
        
        Dialog dialog = Dialog.create(
            factory -> factory.empty().base(builder.build())
                .type(DialogType.dialogList(dialogRegistry).columns(3).build())
        );
        
        sender.audience().showDialog(dialog);
    }
    
    public static class PaperDialogListBuilder extends AbstractDialogListBuilder{
        
        public static PaperDialogListBuilder create(String title){
            return new PaperDialogListBuilder(title);
        }
        
        private final List<String> bodies = new ArrayList<>();
        private final List<AbstractDialog> dialogs = new ArrayList<>();
        
        private PaperDialogListBuilder(String title){
            super(title);
        }
        
        @Override
        public PaperDialogListBuilder appendBody(String body){
            this.bodies.add(body);
            return this;
        }
        
        @Override
        public PaperDialogListBuilder appendDialog(AbstractDialog dialog){
            this.dialogs.add(dialog);
            return this;
        }
        
        @Override
        public PaperDialogList build(){
            return new PaperDialogList(this.title, this.bodies, this.dialogs);
        }
    }
}
