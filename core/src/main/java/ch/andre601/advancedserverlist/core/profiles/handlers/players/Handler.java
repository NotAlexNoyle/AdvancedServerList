package ch.andre601.advancedserverlist.core.profiles.handlers.players;

public enum Handler{
    
    JSON,
    
    MYSQL,
    MARIADB,
    POSTGRESQL,
    
    UNKNOWN;
    
    public static Handler fromString(String key){
        if(key == null || key.isEmpty())
            return UNKNOWN;
        
        for(Handler value : values()){
            if(value.name().equalsIgnoreCase(key))
                return value;
        }
        
        return UNKNOWN;
    }
}
