package centralworks.quests.structure;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.cache.simple.Interpreters;
import centralworks.quests.models.PlayerQuests;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CraftInterpreter<T> {

    private String id;
    private Listener listener;

    public CraftInterpreter(String id) {
        this.id = id;
    }

    public void commit() {
        Bukkit.getPluginManager().registerEvents(listener, Main.getInstance());
        Interpreters.get().add(this);
    }

    public boolean isOnline(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    public PlayerQuests getPlayerQuests(String name) {
        return Caches.getCache(PlayerQuests.class).getIfPresent(name);
    }

}
