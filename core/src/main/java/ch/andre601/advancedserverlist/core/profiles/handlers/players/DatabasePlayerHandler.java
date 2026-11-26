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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class DatabasePlayerHandler extends PlayerHandler{
    
    protected final List<Dependency> dependencies = new ArrayList<>();
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new PlayerCacheThreadFactory());
    private final String dataSourceClass;
    private HikariDataSource dataSource;
    
    public DatabasePlayerHandler(AdvancedServerList<?> core, String dataSourceClass){
        super(core);
        this.dataSourceClass = dataSourceClass;
    }
    
    @Override
    public void load(){
        if(core().fileHandler().getBoolean("disableCache") || dataSource == null)
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
        
        if(dataSource == null)
            return;
        
        dataSource.close();
    }
    
    @Override
    public void add(CachedPlayer player){
        if(core().fileHandler().getBoolean("disableCache") || dataSource == null)
            return;
        
        try(Connection connection = connection();
            PreparedStatement statement = Queries.INSERT_PLAYER_MYSQL.statement(connection)){
            statement.setString(1, player.ip());
            statement.setString(2, player.uuid().toString());
            statement.setString(3, player.name());
            
            statement.executeUpdate();
            
            super.add(player);
        }catch(SQLException ex){
            logger().warn("Encountered SQLException while saving Player %s (%s) to database.", player.name(), player.uuid());
            if(core().fileHandler().getBoolean("debug"))
                logger().warn("Exception:", ex);
        }
    }
    
    @Override
    public boolean remove(CachedPlayer player){
        if(dataSource == null)
            return false;
        
        try(Connection connection = connection();
            PreparedStatement statement = Queries.REMOVE_PLAYER.statement(connection)){
            statement.setString(1, player.uuid().toString());
            
            return statement.executeUpdate() != 0 && super.remove(player);
        }catch(SQLException ex){
            logger().warn("Encountered SQLException while removing Player %s (%s) from database.", player.name(), player.uuid());
            if(core().fileHandler().getBoolean("debug"))
                logger().warn("Exception:", ex);
            
            return false;
        }
    }
    
    public void dependencies(){
        dependencies.add(new Dependency("com.zaxxer", "HikariCP", core().versions().get("hikaricp")));
        dependencies.add(new Dependency("org.flywaydb", "flyway-core", core().versions().get("flyway")));
    }
    
    public boolean setup(){
        try{
            downloadDependencies();
            
            HikariConfig config = new HikariConfig();
            
            config.setDataSourceClassName(dataSourceClass);
            config.setUsername(core().fileHandler().getString("root", "storage", "username"));
            config.setPassword(core().fileHandler().getString("", "storage", "password"));
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
            
            return true;
        }catch(Exception ex){
            logger().warn("Encountered an Exception while setting up Database connection.");
            if(core().fileHandler().getBoolean("debug"))
                logger().warn("Exception:", ex);
            
            return false;
        }
    }
    
    public void loadScheduler(){
        this.scheduler.scheduleAtFixedRate(
            this::load,
            1L,
            core().fileHandler().getLong(5, 1, "database", "refreshRate"),
            TimeUnit.MINUTES
        );
    }
    
    public Connection connection() throws SQLException{
        return dataSource.getConnection();
    }
    
    private void downloadDependencies(){
        for(Dependency dependency : dependencies){
            core().plugin().downloadLibrary(dependency.groupId(), dependency.artifactId(), dependency.version());
        }
    }
    
    private static class PlayerCacheThreadFactory implements ThreadFactory{
        @Override
        public Thread newThread(@NonNull Runnable r){
            Thread t = new Thread(r, "AdvancedServerList-PlayerCacheThread");
            t.setDaemon(true);
            return t;
        }
    }
    
    public record Dependency(String groupId, String artifactId, String version){}
}
