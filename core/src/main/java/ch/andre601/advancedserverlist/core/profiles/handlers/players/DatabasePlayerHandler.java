package ch.andre601.advancedserverlist.core.profiles.handlers.players;

import ch.andre601.advancedserverlist.core.AdvancedServerList;
import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class DatabasePlayerHandler extends PlayerHandler{
    
    private final HikariDataSource dataSource;
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new PlayerCacheThreadFactory());
    
    public DatabasePlayerHandler(AdvancedServerList<?> core, String dataSourceClass){
        super(core);
        
        loadDependencies();
        
        HikariConfig config = new HikariConfig();
        
        config.setDataSourceClassName(dataSourceClass);
        config.setUsername(core.fileHandler().getString("root", "storage", "username"));
        config.setPassword(core.fileHandler().getString("", "storage", "password"));
        config.addDataSourceProperty("serverName", core().fileHandler().getString("localhost", "storage", "url"));
        config.addDataSourceProperty("portNumber", core().fileHandler().getLong(3306, 1, "storage", "port"));
        config.addDataSourceProperty("databaseName", core().fileHandler().getString("playercache", "storage", "name"));
        
        this.dataSource = new HikariDataSource(config);
        
        Flyway.configure()
            .dataSource(dataSource)
            .locations("db_migrations")
            .loggers("slf4j")
            .validateMigrationNaming(true)
            .failOnMissingLocations(true)
            .load()
            .migrate();
        
        this.scheduler.scheduleAtFixedRate(
            this::load,
            1L,
            core.fileHandler().getLong(5, 1, "database", "refreshRate"),
            TimeUnit.MINUTES
        );
    }
    
    @Override
    public void load(){
        if(core().fileHandler().getBoolean("disableCache"))
            return;
        
        try(Connection connection = connection();
            PreparedStatement statement = Queries.GET_PLAYERS.statement(connection)){
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
    
    @Override
    public void save(){
        scheduler.shutdown();
        try{
            if(!scheduler.awaitTermination(1, TimeUnit.SECONDS)){
                scheduler.shutdownNow();
                if(!scheduler.awaitTermination(1, TimeUnit.SECONDS))
                    logger().warn("PlayerCache Scheduler did not terminate in time!");
            }
        }catch(InterruptedException ex){
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void add(CachedPlayer player){
        if(core().fileHandler().getBoolean("disableCache"))
            return;
        
        super.add(player);
        
        try(Connection connection = connection();
            PreparedStatement statement = Queries.INSERT_PLAYER_MYSQL.statement(connection)){
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
        
        try(Connection connection = connection();
            PreparedStatement statement = Queries.REMOVE_PLAYER.statement(connection)){
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
    
    private static class PlayerCacheThreadFactory implements ThreadFactory{
        @Override
        public Thread newThread(@NonNull Runnable r){
            Thread t = new Thread(r, "AdvancedServerList-PlayerCacheThread");
            t.setDaemon(true);
            return t;
        }
    }
}
