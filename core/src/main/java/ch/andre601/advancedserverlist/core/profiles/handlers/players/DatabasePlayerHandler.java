package ch.andre601.advancedserverlist.core.profiles.handlers.players;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class DatabasePlayerHandler extends PlayerHandler{
    
    protected final HikariDataSource dataSource;
    protected final HikariConfig config = new HikariConfig();
    
    public DatabasePlayerHandler(AdvancedServerList<?> core, String dataSourceClass){
        super(core);
        
        loadDependencies();
        
        this.config.setDataSourceClassName(dataSourceClass);
        this.config.setUsername(core.fileHandler().getString("root", "storage", "username"));
        this.config.setPassword(core.fileHandler().getString("", "storage", "password"));
        this.config.addDataSourceProperty("serverName", core().fileHandler().getString("localhost", "storage", "url"));
        this.config.addDataSourceProperty("portNumber", core().fileHandler().getLong(3306, 1, "storage", "port"));
        this.config.addDataSourceProperty("databaseName", core().fileHandler().getString("playercache", "storage", "name"));
        
        
        this.dataSource = new HikariDataSource(config);
        
        Flyway.configure()
            .dataSource(dataSource)
            .locations("db_migrations")
            .loggers("slf4j")
            .validateMigrationNaming(true)
            .failOnMissingLocations(true)
            .load()
            .migrate();
    }
    
    @Override
    public void load(){
        try(PreparedStatement statement = Queries.GET_PLAYERS.statement(connection())){
            try(ResultSet set = statement.executeQuery()){
                players.clear();
                while(set.next()){
                    players.add(new CachedPlayer(
                        set.getString("ip"),
                        set.getString("name"),
                        UUID.fromString(set.getString("uuid"))
                    ));
                }
            }
        }catch(SQLException ex){
            logger().warn("Encountered SQLException while loading players from Database.");
            if(core().fileHandler().getBoolean("debug"))
                logger().warn("Exception:", ex);
        }
    }
    
    // Unused as we directly save to database when adding players.
    @Override
    public void save(){}
    
    @Override
    public void add(CachedPlayer player){
        if(core().fileHandler().getBoolean("disableCache"))
            return;
        
        super.add(player);
        
        try(PreparedStatement statement = Queries.INSERT_PLAYER_MYSQL.statement(connection())){
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
    
    @Override
    public boolean remove(CachedPlayer player){
        super.remove(player);
        
        try(PreparedStatement statement = Queries.REMOVE_PLAYER.statement(connection())){
            statement.setString(1, player.uuid().toString());
            
            return statement.executeUpdate() != 0;
        }catch(SQLException ex){
            logger().warn("Encountered SQLException while removing Player %s (%s) from database.", player.name(), player.uuid());
            if(core().fileHandler().getBoolean("debug"))
                logger().warn("Exception:", ex);
            
            return false;
        }
    }
    
    public void loadDependencies(){
        core().plugin().downloadLibrary("com.zaxxer", "HikariCP", core().versions().get("hikaricp"));
        core().plugin().downloadLibrary("org.flywaydb", "flyway-core", core().versions().get("flyway"));
    }
    
    public Connection connection() throws SQLException{
        return dataSource.getConnection();
    }
}
