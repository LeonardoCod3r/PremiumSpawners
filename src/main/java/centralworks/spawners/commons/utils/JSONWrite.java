package centralworks.spawners.commons.utils;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.specifications.JsonFiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JSONWrite<T> {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public JSONWrite(T object, String fileName) {
        try {
            final File database = new File(Main.get().getDataFolder(), "database");
            final File dir = new File(database, object.getClass().getSimpleName());
            dir.mkdirs();
            final File file = new File(dir, fileName + ".json");
            if (!file.exists()) file.createNewFile();
            final FileWriter writer = new FileWriter(file);
            writer.write(Main.getGson().toJson(object));
            writer.close();
            JsonFiles.getInstance().add(object.getClass().getSimpleName() + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
