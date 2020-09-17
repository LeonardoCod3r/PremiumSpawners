package centralworks.spawners.commons.database;

import centralworks.spawners.commons.database.specifications.AbstractDAO;
import centralworks.spawners.commons.database.specifications.AbstractDTO;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Data
public class QueriesSync<T extends Storable<T>> {

    private String identifier;
    private Integer id;
    private T object;
    private AbstractDTO<T> dto;
    private AbstractDAO<T> dao;

    public QueriesSync(Class<T> classe) {
        dto = new AbstractDTO<>(classe);
        try {
            final T instance = classe.newInstance();
            dao = new AbstractDAO<>(classe, instance.getProperties());
        } catch (Exception ignored) {
        }
    }

    public QueriesSync(Class<T> classe, String identifier) {
        this.identifier = identifier;
        dto = new AbstractDTO<>(classe);
        try {
            final T instance = classe.newInstance();
            dao = new AbstractDAO<>(classe, instance.getProperties());
        } catch (Exception ignored) {
        }
    }

    public QueriesSync(Class<T> classe, Integer id) {
        this.id = id;
        dto = new AbstractDTO<>(classe);
        try {
            final T instance = classe.newInstance();
            dao = new AbstractDAO<>(classe, instance.getProperties());
        } catch (Exception ignored) {
        }
    }

    public QueriesSync(Class<T> classe, T obj) {
        setObject(obj);
        dto = new AbstractDTO<>(classe);
        try {
            final T instance = classe.newInstance();
            dao = new AbstractDAO<>(classe, instance.getProperties());
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static <A extends Storable<A>> QueriesSync<A> supply(A object) {
        return new QueriesSync<>((Class<A>) object.getClass(), object);
    }

    public static <A extends Storable<A>> QueriesSync<A> supply(Class<A> aClass) {
        return new QueriesSync<>(aClass);
    }

    public static <A extends Storable<A>> QueriesSync<A> supply(Class<A> aClass, String id) {
        return new QueriesSync<>(aClass, id);
    }

    public static <A extends Storable<A>> QueriesSync<A> supply(Class<A> aClass, Integer id) {
        return new QueriesSync<>(aClass, id);
    }

    public T persist() {
        if (exists()) queue();
        return object;
    }

    public QueriesSync<T> setObject(T obj) {
        final String s = (String) obj.getIdentifier();
        this.object = obj;
        if (StringUtils.isNumeric(s)) setId(Integer.parseInt(s));
        else setIdentifier(s);
        return this;
    }

    public QueriesSync<T> queue() {
        if (identifier != null) {
            if (dto.exists(identifier)) setObject(dto.read(identifier));
            else {
                final T target = dao.load(identifier);
                dto.write(target);
                setObject(target);
            }
        } else {
            if (dto.exists(id)) setObject(dto.read(id));
            else {
                final T target = dao.load(id);
                dto.write(target);
                setObject(target);
            }
        }
        return this;
    }

    public QueriesSync<T> queue(Consumer<T> success) {
        if (identifier != null) {
            if (dto.exists(identifier)) setObject(dto.read(identifier));
            else {
                final T target = dao.load(identifier);
                dto.write(target);
                setObject(target);
            }
        } else {
            if (dto.exists(id)) setObject(dto.read(id));
            else {
                final T target = dao.load(id);
                dto.write(target);
                setObject(target);
            }
        }
        success.accept(getObject());
        return this;
    }

    public QueriesSync<T> queue(BiConsumer<T, QueriesSync<T>> success) {
        if (identifier != null) {
            if (dto.exists(identifier)) setObject(dto.read(identifier));
            else {
                final T target = dao.load(identifier);
                dto.write(target);
                setObject(target);
            }
        } else {
            if (dto.exists(id)) setObject(dto.read(id));
            else {
                final T target = dao.load(id);
                dto.write(target);
                setObject(target);
            }
        }
        success.accept(getObject(), this);
        return this;
    }

    public QueriesSync<T> queue(Consumer<T> success, Consumer<Exception> error) {
        if (identifier != null) {
            try {
                if (dto.exists(identifier)) setObject(dto.read(identifier));
                else {
                    final T target = dao.load(identifier);
                    dto.write(target);
                    setObject(target);
                }
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e);
            }
        } else {
            try {
                if (dto.exists(id)) setObject(dto.read(id));
                else {
                    final T target = dao.load(id);
                    dto.write(target);
                    setObject(target);
                }
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e);
            }
        }
        return this;
    }

    public QueriesSync<T> queue(Consumer<T> success, BiConsumer<Exception, QueriesSync<T>> error) {
        if (identifier != null) {
            try {
                if (dto.exists(identifier)) setObject(dto.read(identifier));
                else {
                    final T target = dao.load(identifier);
                    dto.write(target);
                    setObject(target);
                }
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e, this);
            }
        } else {
            try {
                if (dto.exists(id)) setObject(dto.read(id));
                else {
                    final T target = dao.load(id);
                    dto.write(target);
                    setObject(target);
                }
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e, this);
            }
        }
        return this;
    }

    public QueriesSync<T> lazyQueue() {
        if (identifier != null) {
            if (dto.exists(identifier)) setObject(dto.read(identifier));
            else setObject(dao.load(identifier));
        } else {
            if (dto.exists(id)) setObject(dto.read(id));
            else setObject(dao.load(id));
        }
        return this;
    }

    public QueriesSync<T> lazyQueue(Consumer<T> success) {
        if (identifier != null) {
            if (dto.exists(identifier)) setObject(dto.read(identifier));
            else setObject(dao.load(identifier));
        } else {
            if (dto.exists(id)) setObject(dto.read(id));
            else setObject(dao.load(id));
        }
        success.accept(getObject());
        return this;
    }

    public QueriesSync<T> lazyQueue(BiConsumer<T, QueriesSync<T>> success) {
        if (identifier != null) {
            if (dto.exists(identifier)) setObject(dto.read(identifier));
            else setObject(dao.load(identifier));
        } else {
            if (dto.exists(id)) setObject(dto.read(id));
            else setObject(dao.load(id));
        }
        success.accept(getObject(), this);
        return this;
    }

    public QueriesSync<T> lazyQueue(Consumer<T> success, Consumer<Exception> error) {
        if (identifier != null) {
            try {
                if (dto.exists(identifier)) setObject(dto.read(identifier));
                else setObject(dao.load(identifier));
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e);
            }
        } else {
            try {
                if (dto.exists(id)) setObject(dto.read(id));
                else setObject(dao.load(id));
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e);
            }
        }
        return this;
    }

    public QueriesSync<T> lazyQueue(Consumer<T> success, BiConsumer<Exception, QueriesSync<T>> error) {
        if (identifier != null) {
            try {
                if (dto.exists(identifier)) setObject(dto.read(identifier));
                else setObject(dao.load(identifier));
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e, this);
            }
        } else {
            try {
                if (dto.exists(id)) setObject(dto.read(id));
                else setObject(dao.load(id));
                success.accept(getObject());
            } catch (Exception e) {
                error.accept(e, this);
            }
        }
        return this;
    }

    public QueriesSync<T> execute(Consumer<QueriesSync<T>> q) {
        q.accept(this);
        return this;
    }

    public boolean exists() {
        if (identifier != null) return dto.exists(identifier) || dao.exists(identifier);
        else return dto.exists(id) || dao.exists(id);
    }

    public QueriesSync<T> ifExists(Consumer<T> success) {
        if (exists()) queue(success);
        return this;
    }

    public QueriesSync<T> ifExists(Consumer<T> success, Consumer<Exception> error) {
        if (exists()) queue(success, error);
        return this;
    }

    public QueriesSync<T> delete(Boolean... booleans) {
        final boolean v = booleans.length > 0 && booleans[0];
        if (identifier != null) {
            dto.delete(identifier);
            if (v) dao.delete(identifier);
        } else {
            dto.delete(id);
            if (v) dao.delete(id);
        }
        return this;
    }

    public QueriesSync<T> commit(Boolean... booleans) {
        final boolean v = booleans.length > 0 && booleans[0];
        if (getObject() == null) queue((t, tQueriesSync) -> {
            tQueriesSync.dto.write(t);
            if (v) tQueriesSync.dao.save(t);
        });
        else {
            dto.write(getObject());
            if (v) dao.save(getObject());
        }
        return this;
    }


}
