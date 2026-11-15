package ch.andre601.advancedserverlist.core.profiles.handlers.players.storage;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.DatabasePlayerHandler;

public class MariaDBPlayerHandler extends DatabasePlayerHandler{
    
    public MariaDBPlayerHandler(AdvancedServerList<?> core){
        super(core, "org.mariadb.jdbc.MariaDbDataSource");
    }
    
    
    @Override
    public void loadDependencies(){
        core().plugin().downloadLibrary("org.mariadb.jdbc", "mariadb-java-client", core().versions().get("mariadb"));
        
        super.loadDependencies();
    }
}
