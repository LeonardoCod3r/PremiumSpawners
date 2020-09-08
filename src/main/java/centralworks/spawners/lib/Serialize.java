package centralworks.spawners.lib;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Serialize<T, V> {

    private final T toSerialize;
    private V result;

    public Serialize(T toSerialize) {
        this.toSerialize = toSerialize;
    }

    @SuppressWarnings("unchecked")
    public void serialize() {
        if (toSerialize.getClass() == Location.class) {
            final Location location = (Location) toSerialize;
            result = (V) (location.getX() + "__" + location.getY() + "__" + location.getZ() + "__" + location.getWorld().getName() + "__" + location.getYaw() + "__" + location.getPitch());
        } else if (toSerialize.getClass() == String.class) {
            final String location = String.valueOf(toSerialize);
            result = (V) new Location(Bukkit.getWorld(location.split("__")[3]), Double.parseDouble(location.split("__")[0]), Double.parseDouble(location.split("__")[1]), Double.parseDouble(location.split("__")[2]), Float.parseFloat(location.split("__")[4]), Float.parseFloat(location.split("__")[5]));
        }
    }

    public V getResult() {
        if (result == null) serialize();
        return result;
    }
}
