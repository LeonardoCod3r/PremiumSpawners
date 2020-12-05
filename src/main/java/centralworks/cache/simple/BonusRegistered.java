package centralworks.cache.simple;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.Map;

public class BonusRegistered {

    private static final Map<String, Integer> bonus = Maps.newHashMap();

    private BonusRegistered() {
        throw new IllegalStateException("Utility class");
    }

    public static void put(String permission, Integer discount) {
        bonus.put(permission, discount);
    }

    public static Integer get(Player p) {
        return bonus.getOrDefault(bonus.keySet().stream().filter(p::hasPermission).findFirst().orElse(""), 0);
    }
}
