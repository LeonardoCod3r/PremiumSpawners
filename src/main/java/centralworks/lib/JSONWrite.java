package centralworks.lib;

import centralworks.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JSONWrite<T> {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public JSONWrite(T object, String fileName) {
        try {
            final File database = new File(Main.getInstance().getDataFolder(), "database");
            final File dir = new File(database, object.getClass().getSimpleName());
            dir.mkdirs();
            final File file = new File(dir, fileName + ".json");
            if (!file.exists()) file.createNewFile();
            final FileWriter writer = new FileWriter(file);
            writer.write(Main.getGson().toJson(object));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
