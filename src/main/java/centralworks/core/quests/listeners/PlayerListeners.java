package centralworks.core.quests.listeners;

import centralworks.cache.Caches;
import centralworks.core.quests.models.PlayerQuests;
import com.google.common.cache.LoadingCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerListeners implements Listener {

    @EventHandler
    public void join(PlayerJoinEvent event) {
        final LoadingCache<String, PlayerQuests> cache = Caches.getCache(PlayerQuests.class);
        final String name = event.getPlayer().getName();
        final PlayerQuests playerQuests = Optional.ofNullable(cache.getIfPresent(name)).orElse(new PlayerQuests(name));
        playerQuests.addQuestsDefaults();
        cache.put(name, playerQuests);
    }
}
