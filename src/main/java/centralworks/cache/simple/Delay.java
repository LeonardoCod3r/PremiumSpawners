package centralworks.cache.simple;

import com.google.common.collect.Maps;

import java.util.HashMap;

public class Delay {

    private static final HashMap<String, Long> map;
    private static final Long delay;

    static {
        map = Maps.newHashMap();
        delay = 30 * 1000L;
    }

    public static void put(String playerName) {
        if (map.containsKey(playerName.toLowerCase()))
            map.replace(playerName.toLowerCase(), System.currentTimeMillis());
        else map.put(playerName.toLowerCase(), System.currentTimeMillis());
    }

    public static boolean inDelay(String playerName) {
        if (!map.containsKey(playerName.toLowerCase())) {
            return false;
        }
        final boolean inDelay = map.get(playerName.toLowerCase()) + delay > System.currentTimeMillis();
        if (inDelay) map.remove(playerName.toLowerCase());
        return inDelay;
    }


}
