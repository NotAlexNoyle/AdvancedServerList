package ch.andre601.advancedserverlist.core.compat.maintenance;

import ch.andre601.advancedserverlist.api.PlaceholderProvider;
import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import ch.andre601.advancedserverlist.core.AdvancedServerList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MaintenancePlaceholder extends PlaceholderProvider{
    
    private final AdvancedServerList<?> plugin;
    
    public MaintenancePlaceholder(AdvancedServerList<?> plugin){
        super("maintenance");
        
        this.plugin = plugin;
    }
    @Override
    public @Nullable String parsePlaceholder(String placeholder, GenericPlayer player, GenericServer server){
        if(!placeholder.equals("maintenanceEnabled") || !plugin.plugin().isPluginEnabled("Maintenance"))
            return null;
        
        return String.valueOf(MaintenanceUtil.get().isMaintenanceEnabled());
    }
}
