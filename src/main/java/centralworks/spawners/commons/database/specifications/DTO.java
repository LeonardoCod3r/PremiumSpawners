package centralworks.spawners.commons.database.specifications;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.Storable;
import centralworks.spawners.commons.utils.JSONRead;
import centralworks.spawners.commons.utils.JSONWrite;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DTO<T extends Storable<T>> {

    private final Class<T> clazz;

    public DTO(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void write(T object) {
        final String id = (String) object.getIdentifier();
        new JSONWrite<>(object, id);
    }

    public T read(String id) {
        return new JSONRead<>(getClazz(), id).getObject();
    }

    public T read(Integer id) {
        return read(id.toString());
    }

    public boolean exists(String id) {
        return JsonFiles.getInstance().exists(clazz.getSimpleName() + id);
    }

    public boolean exists(Integer id) {
        return exists(id.toString());
    }

    public List<T> loadAllFiles() {
        try {
            final List<T> ts = new DAO<>(clazz, clazz.newInstance().getProperties()).loadAll();
            ts.forEach(t -> {
                final String id = (String) t.getIdentifier();
                Bukkit.getScheduler().runTask(Main.get(), () -> read(id));
            });
            return ts;
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void delete(String id) {
        final File database = new File(Main.get().getDataFolder(), "database");
        final File dir = new File(database, getClazz().getSimpleName());
        new File(dir, id + ".json").delete();
        JsonFiles.getInstance().remove(clazz.getSimpleName() + id);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void delete(File file) {
        JsonFiles.getInstance().remove(clazz.getSimpleName() + file.getName().split(".json")[0]);
        file.delete();
    }

    public void delete(Integer id) {
        delete(id.toString());
    }

    public void delete() {
        try {
            final File database = new File(Main.get().getDataFolder(), "database");
            final File dir = new File(database, getClazz().getSimpleName());
            Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(this::delete);
        } catch (NullPointerException ignored) {
        }
    }

    public List<T> findAllFiles() {
        try {
            final File database = new File(Main.get().getDataFolder(), "database");
            final File dir = new File(database, getClazz().getSimpleName());
            return Arrays.stream(Objects.requireNonNull(dir.listFiles())).map(file -> new JSONRead<>(getClazz(), file.getName().split(".json")[0]).getObject()).collect(Collectors.toList());
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }

}
