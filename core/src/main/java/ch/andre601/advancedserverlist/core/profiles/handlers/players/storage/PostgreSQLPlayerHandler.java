package ch.andre601.advancedserverlist.core.profiles.handlers.players.storage;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.DatabasePlayerHandler;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.Queries;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgreSQLPlayerHandler extends DatabasePlayerHandler{
    
    public PostgreSQLPlayerHandler(AdvancedServerList<?> core){
        super(core, "org.postgresql.ds.PGSimpleDataSource");
    }
    
    @Override
    public void loadDependencies(){
        core().plugin().downloadLibrary("org.postgresql", "postgresql", core().versions().get("postgres"));
        core().plugin().downloadLibrary("org.flywaydb", "flyway-database-postgresql", core().versions().get("flyway"));
        
        super.loadDependencies();
    }
    
    @Override
    public void add(CachedPlayer player){
        if(core().fileHandler().getBoolean("disableCache"))
            return;
        
        super.add(player);
        
        try(PreparedStatement statement = Queries.INSERT_PLAYER_POSTGRES.statement(connection())){
            statement.setString(1, player.ip());
            statement.setString(2, player.uuid().toString());
            statement.setString(3, player.name());
            
            statement.executeUpdate();
        }catch(SQLException ex){
            logger().warn("Encountered SQLException while saving Player %s (%s) to database.", player.name(), player.uuid());
            if(core().fileHandler().getBoolean("debug"))
                logger().warn("Exception:", ex);
        }
    }
}
