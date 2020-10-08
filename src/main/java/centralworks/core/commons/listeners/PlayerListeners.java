package centralworks.core.commons.listeners;

import centralworks.core.commons.models.UserDetails;
import centralworks.database.SyncRequests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final String name = e.getPlayer().getName();
        final SyncRequests<UserDetails, Object> query = new UserDetails(name).query();
        query.persist().fixLimits();
        query.commit();
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final String name = e.getPlayer().getName();
        final SyncRequests<UserDetails, Object> query = new UserDetails(name).query();
        query.persist();
        query.commit();
    }

}
