package centralworks.core.quests.listeners;

import centralworks.database.SyncRequests;
import centralworks.core.quests.models.PlayerQuests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event) {
        final SyncRequests<PlayerQuests, Object> query2 = new PlayerQuests(event.getPlayer().getName()).query();
        query2.persist().addQuestsDefaults();
        query2.commit();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        final SyncRequests<PlayerQuests, Object> query2 = new PlayerQuests(event.getPlayer().getName()).query();
        query2.persist();
        query2.commit();
    }

}
