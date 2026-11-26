package ch.andre601.advancedserverlist.core;

import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.Queries;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class AbstractDatabaseStorageTests{
    
    protected HikariDataSource dataSource;
    
    protected abstract void setupDatabase() throws SQLException;
    
    protected boolean setupFlyway(){
        try{
            Flyway.configure()
                .dataSource(dataSource)
                .locations("db_migrations")
                .loggers("slf4j")
                .validateMigrationNaming(true)
                .failOnMissingLocations(true)
                .load()
                .migrate();
            
            return true;
        }catch(FlywayException ex){
            return false;
        }
    }
    
    @AfterEach
    void finish(){
        if(dataSource != null)
            dataSource.close();
    }
    
    protected boolean add(CachedPlayer player, Queries query){
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = query.statement(connection)){
            statement.setString(1, player.ip());
            statement.setString(2, player.uuid().toString());
            statement.setString(3, player.name());
            
            return statement.executeUpdate() != 0;
        }catch(SQLException ex){
            return false;
        }
    }
    
    protected CachedPlayer get(){
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = Queries.GET_PLAYERS.statement(connection)){
            try(ResultSet set = statement.executeQuery()){
                while(set.next()){
                    UUID uuid;
                    try{
                        uuid = UUID.fromString(set.getString("uuid"));
                    }catch(IllegalArgumentException ex){
                        continue;
                    }
                    
                    return  new CachedPlayer(
                        set.getString("ip"),
                        set.getString("name"),
                        uuid
                    );
                }
                
                return null;
            }
        }catch(SQLException ex){
            return null;
        }
    }
    
    protected boolean remove(CachedPlayer player){
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = Queries.REMOVE_PLAYER.statement(connection)){
            statement.setString(1, player.uuid().toString());
            
            return statement.executeUpdate() != 0;
        }catch(SQLException ex){
            return false;
        }
    }
    
    protected CachedPlayer player(){
        return player("127.0.0.1", "Test");
    }
    
    protected CachedPlayer player(String ip, String name){
        return new CachedPlayer(ip, name, UUID.randomUUID());
    }
}
