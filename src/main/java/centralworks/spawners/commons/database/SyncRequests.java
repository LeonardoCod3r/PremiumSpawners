package centralworks.spawners.commons.database;

import centralworks.spawners.commons.database.specifications.Repository;
import lombok.Data;
import lombok.val;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Data
public class SyncRequests<O extends Storable<O>, T> {

    private Class<O> classFile;
    private String identifier;
    private Repository<O, T> repository;
    private O target;

    public static <A extends Storable<A>, B> SyncRequests<A, B> supply(Repository<A, B> repository, A object) {
        return new SyncRequests<>(repository, object);
    }

    public static <A extends Storable<A>, B> SyncRequests<A, B> supply(Repository<A, B> repository) {
        return new SyncRequests<>(repository);
    }

    public static <A extends Storable<A>, B> SyncRequests<A, B> supply(Repository<A, B> repository, String id) {
        return new SyncRequests<>(repository, id);
    }

    public SyncRequests(Repository<O, T> repository) {
        this.classFile = repository.getTarget();
        this.repository = repository;
    }

    public SyncRequests(Repository<O, T> repository, String identifier) {
        this.classFile = repository.getTarget();
        this.repository = repository;
        this.identifier = identifier;
    }

    public SyncRequests(Repository<O, T> repository, O target) {
        this.classFile = repository.getTarget();
        this.repository = repository;
        this.target = target;
        this.identifier = (String) target.getEntityIdentifier();
    }

    public O persist() {
        if (exists()) queue();
        return target;
    }

    public SyncRequests<O, T> setTarget(O obj) {
        final String s = (String) obj.getEntityIdentifier();
        this.target = obj;
        setIdentifier(s);
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> queue() {
            val target = repository.read((T)identifier);
            target.ifPresent(this::setTarget);

        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> queue(Consumer<O> success) {
            val target = repository.read((T)identifier);
            target.ifPresent(this::setTarget);
        success.accept(getTarget());
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> queue(BiConsumer<O, SyncRequests<O, T>> success) {
            val target = repository.read((T)identifier);
            target.ifPresent(this::setTarget);
        success.accept(getTarget(), this);
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> queue(Consumer<O> success, Consumer<Exception> error) {
        try {
                val target = repository.read((T)identifier);
                target.ifPresent(this::setTarget);
            success.accept(getTarget());
        } catch (Exception e) {
            error.accept(e);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> queue(Consumer<O> success, BiConsumer<Exception, SyncRequests<O, T>> error) {
        try {
                val target = repository.read((T)identifier);
                target.ifPresent(this::setTarget);
            success.accept(getTarget());
        } catch (Exception e) {
            error.accept(e, this);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> lazyQueue() {
        setTarget(repository.read((T)identifier).get());
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> lazyQueue(Consumer<O> success) {
        setTarget(repository.read((T)identifier).get());
        success.accept(getTarget());
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> lazyQueue(BiConsumer<O, SyncRequests<O, T>> success) {
        setTarget(repository.read((T) identifier).get());
        success.accept(getTarget(), this);
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> lazyQueue(Consumer<O> success, Consumer<Exception> error) {
        try {
            setTarget(repository.read((T)identifier).get());
            success.accept(getTarget());
        } catch (Exception e) {
            error.accept(e);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public SyncRequests<O, T> lazyQueue(Consumer<O> success, BiConsumer<Exception, SyncRequests<O, T>> error) {
        try {
            setTarget(repository.read((T)identifier).get());
            success.accept(getTarget());
        } catch (Exception e) {
            error.accept(e, this);
        }
        return this;
    }

    public SyncRequests<O, T> execute(Consumer<SyncRequests<O, T>> q) {
        q.accept(this);
        return this;
    }

    public SyncRequests<O, T> ifExists(Consumer<O> success) {
        if (exists()) queue(success);
        return this;
    }

    public SyncRequests<O, T> ifExists(Consumer<O> success, Consumer<Exception> error) {
        if (exists()) queue(success, error);
        return this;
    }

    public SyncRequests<O, T> delete() {
        repository.delete(target);
        return this;
    }

    @SuppressWarnings("unchecked")
    public boolean exists() {
        return repository.exists((T) identifier);
    }

    public SyncRequests<O, T> commit() {
        if (getTarget() == null) queue((t, tQueriesSync) -> tQueriesSync.repository.commit(t));
        else repository.commit(getTarget());
        return this;
    }

}
