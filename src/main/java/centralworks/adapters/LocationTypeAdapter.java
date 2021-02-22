package centralworks.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationTypeAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter out, Location loc) throws IOException {
        out.beginObject();
        out.name("world").value(loc.getWorld().getName());
        out.name("x").value(loc.getX());
        out.name("y").value(loc.getY());
        out.name("z").value(loc.getZ());
        out.name("pitch").value(loc.getPitch());
        out.name("yaw").value(loc.getYaw());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        var world = "";
        var x = 0.0;
        var y = 0.0;
        var z = 0.0;
        var yaw = 0.0f;
        var pitch = 0.0f;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "world":
                    world = in.nextString();
                    break;
                case "x":
                    x = in.nextDouble();
                    break;
                case "y":
                    y = in.nextDouble();
                    break;
                case "z":
                    z = in.nextDouble();
                    break;
                case "yaw":
                    yaw = (float) in.nextDouble();
                    break;
                case "pitch":
                    pitch = (float) in.nextDouble();
            }
        }
        in.endObject();
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
}
