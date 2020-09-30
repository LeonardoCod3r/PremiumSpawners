package centralworks.spawners.modules.models.spawners.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Cache;
import org.bukkit.Bukkit;

public class DCached extends Cache<String> {

    private static DCached me;

    public static DCached get() {
        return me == null ? me = new DCached() : me;
    }

    @Override
    public void add(String object) {
        getList().add(object);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> getList().remove(object), 30 * 20L);
    }
}
