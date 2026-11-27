package ch.andre601.advancedserverlist.core;

import ch.andre601.advancedserverlist.core.objects.CachedPlayer;
import ch.andre601.advancedserverlist.core.profiles.handlers.players.Queries;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class MariaDBStorageTests extends AbstractDatabaseStorageTests{
    
    @Container
    static MariaDBContainer database = new MariaDBContainer("mariadb:11.4")
        .withDatabaseName("playercache")
        .withUsername("test")
        .withPassword("test");
    
    @Override
    protected void setupDatabase() throws SQLException{
        MariaDbDataSource source = new MariaDbDataSource();
        source.setUrl(database.getJdbcUrl());
        source.setUser(database.getUsername());
        source.setPassword(database.getPassword());
        
        HikariConfig config = new HikariConfig();
        config.setDataSource(source);
        
        this.dataSource = new HikariDataSource(config);
        
        assertTrue(setupFlyway(), "Flyway setup failed.");
    }
    
    @Test
    void testPlayerAddPersistence(){
        CachedPlayer player = player();
        
        assertTrue(add(player, Queries.INSERT_PLAYER_MYSQL));
        
        assertEquals(player, get());
    }
    
    @Test
    void testPlayerRemove(){
        CachedPlayer player = player();
        
        assertTrue(add(player, Queries.INSERT_PLAYER_MYSQL));
        
        assertEquals(player, get());
        
        assertTrue(remove(player));
        
        assertNull(get());
    }
}
