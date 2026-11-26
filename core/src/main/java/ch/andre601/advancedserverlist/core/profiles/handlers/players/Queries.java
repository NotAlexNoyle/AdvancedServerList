package ch.andre601.advancedserverlist.core.profiles.handlers.players;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum Queries{
    INSERT_PLAYER_MYSQL(
        "INSERT INTO player_cache (ip, uuid, name) " +
        "VALUES(?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE uuid = VALUE(ip), name = VALUE(name);"
    ),
    INSERT_PLAYER_POSTGRES(
        "INSERT INTO player_cache (ip, uuid, name) " +
        "VALUES(?, ?, ?) " +
        "ON CONFLICT (ip) DO UPDATE SET uuid = EXCLUDED.uuid, name = EXCLUDED.name;"
    ),
    REMOVE_PLAYER(
        "DELETE FROM player_cache WHERE uuid = ?;"
    ),
    GET_PLAYERS(
        "SELECT * FROM player_cache;"
    );
    
    private final String query;
    
    Queries(String query){
        this.query = query;
    }
    
    public PreparedStatement statement(Connection connection) throws SQLException{
        return connection.prepareStatement(this.query);
    }
}
