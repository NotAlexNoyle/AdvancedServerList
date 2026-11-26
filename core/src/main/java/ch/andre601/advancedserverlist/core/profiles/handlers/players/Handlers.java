package ch.andre601.advancedserverlist.core.profiles.handlers.players;

public enum Handlers{
    
    JSON,
    
    MYSQL,
    MARIADB,
    POSTGRESQL,
    
    UNKNOWN;
    
    public static Handlers fromString(String key){
        if(key == null || key.isEmpty())
            return UNKNOWN;
        
        for(Handlers value : values()){
            if(value.name().equalsIgnoreCase(key))
                return value;
        }
        
        return UNKNOWN;
    }
}
