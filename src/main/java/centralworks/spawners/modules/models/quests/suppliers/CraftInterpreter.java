package centralworks.spawners.modules.models.quests.suppliers;

import centralworks.spawners.Main;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import centralworks.spawners.modules.models.quests.cached.Interpreters;
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
        Bukkit.getPluginManager().registerEvents(listener, Main.get());
        Interpreters.get().add(this);
    }

    public boolean isOnline(String name) {
        return Bukkit.getPlayer(name) != null;
    }

    public PlayerQuests getPlayerQuests(String name) {
        return new PlayerQuests(name).query().persist();
    }

}
