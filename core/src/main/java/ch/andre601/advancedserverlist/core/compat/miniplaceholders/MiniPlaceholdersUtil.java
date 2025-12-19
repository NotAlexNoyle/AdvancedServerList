package ch.andre601.advancedserverlist.core.compat.miniplaceholders;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MiniPlaceholdersUtil{
    
    public static TagResolver tagResolvers(){
        return MiniPlaceholders.globalPlaceholders();
    }
}
