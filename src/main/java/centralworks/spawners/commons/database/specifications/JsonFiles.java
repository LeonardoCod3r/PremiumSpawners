package centralworks.spawners.commons.database.specifications;

import com.google.common.collect.Lists;

import java.util.List;


public class JsonFiles {

    private static JsonFiles instance;
    private final List<String> loaded = Lists.newArrayList();

    public static JsonFiles getInstance() {
        if (instance == null) instance = new JsonFiles();
        return instance;
    }

    public void add(String file) {
        loaded.add(file);
    }

    public void remove(String file) {
        loaded.remove(file);
    }

    public boolean exists(String file) {
        return loaded.contains(file);
    }


}
