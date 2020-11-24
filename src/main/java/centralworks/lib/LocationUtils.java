package centralworks.lib;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    public static String locToString(Location location) {
        return location.getX() + "__" + location.getY() + "__" + location.getZ() + "__" + location.getWorld().getName() + "__" + location.getYaw() + "__" + location.getPitch();
    }

    public static Location stringToLoc(String location) {
        return new Location(Bukkit.getWorld(location.split("__")[3]), Double.parseDouble(location.split("__")[0]), Double.parseDouble(location.split("__")[1]), Double.parseDouble(location.split("__")[2]), Float.parseFloat(location.split("__")[4]), Float.parseFloat(location.split("__")[5]));
    }
}
