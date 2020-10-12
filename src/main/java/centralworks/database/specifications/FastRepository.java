package centralworks.database.specifications;

import centralworks.Main;
import centralworks.database.Storable;
import centralworks.lib.json.JSONRead;
import centralworks.lib.json.JSONWrite;
import com.google.common.collect.Lists;
import lombok.Getter;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FastRepository<O extends Storable<O>, T> implements Repository<O, T> {

    @Getter
    private final Class<O> target;

    public FastRepository(Class<O> target) {
        this.target = target;
    }

    @Override
    public O commit(O obj) {
        final String id = (String)obj.getEntityIdentifier();
        new JSONWrite<>(obj, id);
        return obj;
    }

    @Override
    public Optional<O> read(T id) {
        return Optional.ofNullable(new JSONRead<>(getTarget(), (String)id).getObject());
    }

    @Override
    public O update(O obj) {
        return commit(obj);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void delete(O obj) {
        final File database = new File(Main.getInstance().getDataFolder(), "database");
        final File dir = new File(database, getTarget().getSimpleName());
        new File(dir, obj.getEntityIdentifier() + ".json").delete();
    }

    @Override
    public Optional<O> findAndDelete(T id) {
        final Optional<O> result = read(id);
        deleteOf(id, "");
        return result;
    }

    @Override
    public void deleteOf(T id, String idName) {
        final File database = new File(Main.getInstance().getDataFolder(), "database");
        final File dir = new File(database, getTarget().getSimpleName());
        new File(dir, id + ".json").delete();
    }

    @Override
    public List<O> findAll() {
        try {
            final File database = new File(Main.getInstance().getDataFolder(), "database");
            final File dir = new File(database, getTarget().getSimpleName());
            return Arrays.stream(Objects.requireNonNull(dir.listFiles())).map(file -> new JSONRead<>(getTarget(), file.getName().split(".json")[0]).getObject()).collect(Collectors.toList());
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<O> findAll(Predicate<O> predicate) {
        return findAll().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public boolean exists(T id, String idName) {
        return exists(id);
    }

    @Override
    public boolean exists(T id) {
        final File database = new File(Main.getInstance().getDataFolder(), "database");
        final File dir = new File(database, getTarget().getSimpleName());
        return dir.exists();
    }

    @Override
    public Query createQuery(String query) {
        return null;
    }

    @Override
    public Query createNativeQuery(String query) {
        return null;
    }

    @Override
    public Query createQuery(CriteriaQuery<O> criteriaQuery) {
        return null;
    }

}
