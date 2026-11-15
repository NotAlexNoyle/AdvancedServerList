package ch.andre601.advancedserverlist.core.profiles.handlers.players.storage;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.DatabasePlayerHandler;

public class PostgreSQLPlayerHandler extends DatabasePlayerHandler{
    
    public PostgreSQLPlayerHandler(AdvancedServerList<?> core){
        super(core, "org.postgresql.ds.PGSimpleDataSource");
    }
    
    @Override
    public void loadDependencies(){
        core().plugin().downloadLibrary("org.postgresql", "postgresql", core().versions().get("postgres"));
        
        super.loadDependencies();
    }
}
