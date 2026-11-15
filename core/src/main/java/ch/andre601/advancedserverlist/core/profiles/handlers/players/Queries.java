package ch.andre601.advancedserverlist.core.profiles.handlers.players;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum Queries{
    INSERT_PLAYER(
        "INSERT INTO player_cache ('ip', 'uuid', 'name') VALUES(?, ?, ?) ON CONFLICT DO NOTHING;"
    ),
    REMOVE_PLAYER(
        "DELETE FROM player_cache WHERE ip = ?;"
    ),
    UPDATE_PLAYER(
        "UPDATE player_cache SET ip = ?, uuid = ?, name = ? WHERE ip = ?;"
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
