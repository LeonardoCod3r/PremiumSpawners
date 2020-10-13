package centralworks.core.commons.listeners;

import centralworks.cache.Caches;
import centralworks.core.commons.models.UserDetails;
import com.google.common.cache.LoadingCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final LoadingCache<String, UserDetails> cache = Caches.getCache(UserDetails.class);
        final String name = e.getPlayer().getName();
        final UserDetails user = Optional.ofNullable(cache.getIfPresent(name)).orElse(new UserDetails(name));
        user.fixLimits();
        cache.put(name, user);
    }

}
