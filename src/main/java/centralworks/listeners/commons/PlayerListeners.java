package centralworks.listeners.commons;

import centralworks.cache.google.Caches;
import centralworks.models.User;
import com.google.common.cache.LoadingCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final LoadingCache<String, User> cache = Caches.getCache(User.class);
        final String name = e.getPlayer().getName();
        final User user = Optional.ofNullable(cache.getIfPresent(name)).orElse(new User(name));
        user.fixLimits();
        cache.put(name, user);
    }

}
