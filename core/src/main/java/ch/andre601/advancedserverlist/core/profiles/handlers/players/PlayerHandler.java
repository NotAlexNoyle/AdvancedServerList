package ch.andre601.advancedserverlist.core.profiles.handlers.players;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.interfaces.PluginLogger;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class PlayerHandler{
    
    private final AdvancedServerList<?> core;
    private final PluginLogger logger;
    
    protected final UUID defaultUUID = UUID.fromString("606e2ff0-ed77-4842-9d6c-e1d3321c7838");
    protected List<CachedPlayer> players = new ArrayList<>();
    
    private CachedPlayer defaultPlayer = null;
    
    public PlayerHandler(AdvancedServerList<?> core){
        this.core = core;
        this.logger = core.plugin().pluginLogger();
    }
    
    public abstract void load();
    
    public abstract void save();
    
    public CachedPlayer player(String ip){
        if(core.fileHandler().getBoolean("disableCache"))
            return defaultPlayer();
        
        for(CachedPlayer player : players){
            if(player.ip().equalsIgnoreCase(ip))
                return player;
        }
        
        return defaultPlayer();
    }
    
    public CachedPlayer player(UUID uuid){
        for(CachedPlayer player : players){
            if(player.uuid().equals(uuid))
                return player;
        }
        
        return defaultPlayer();
    }
    
    public void add(CachedPlayer player){
        if(core().fileHandler().getBoolean("disableCache"))
            return;
        
        this.players.add(player);
    }
    
    public void remove(CachedPlayer player){
        this.players.remove(player);
    }
    
    public void clear(){
        this.players.clear();
        this.defaultPlayer = null;
    }
    
    public AdvancedServerList<?> core(){
        return this.core;
    }
    
    public PluginLogger logger(){
        return logger;
    }
    
    public CachedPlayer defaultPlayer(){
        if(defaultPlayer != null)
            return defaultPlayer;
        
        return (defaultPlayer = new CachedPlayer(
            "0.0.0.0",
            core.fileHandler().getString("Anonymous", "unknownPlayer", "name"),
            defaultUUID()
        ));
    }
    
    private UUID defaultUUID(){
        try{
            return UUID.fromString(core.fileHandler().getString(defaultUUID.toString(), "unknownPlayer", "uuid"));
        }catch(IllegalArgumentException ex){
            return defaultUUID;
        }
    }
}
