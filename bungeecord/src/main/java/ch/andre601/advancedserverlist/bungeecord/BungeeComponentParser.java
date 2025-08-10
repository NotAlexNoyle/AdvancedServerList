package ch.andre601.advancedserverlist.bungeecord;

import ch.andre601.advancedserverlist.core.parsing.ComponentParser;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class BungeeComponentParser{
    
    public static BaseComponent parse(String text){
        return new ComponentBuilder()
            .append(BungeeComponentSerializer.get().serialize(ComponentParser.text(text).toComponent()))
            .build();
    }
}
