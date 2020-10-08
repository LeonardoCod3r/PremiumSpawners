package centralworks.core.dropstorage.cache;

import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BonusRegistered {

    public static HashMap<String, Integer> bonus = Maps.newHashMap();

    public static void put(String permission, Integer discount) {
        bonus.put(permission, discount);
    }

    public static Integer get(Player p) {
        if (bonus.keySet().stream().anyMatch(p::hasPermission))
            return bonus.get(bonus.keySet().stream().filter(p::hasPermission).findFirst().get());
        else return 0;
    }
}
