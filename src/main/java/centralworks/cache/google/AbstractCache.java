package centralworks.cache.google;

import centralworks.database.Storable;

import java.util.Optional;

public abstract class AbstractCache<O extends Storable<O>> implements Cache<O> {

    public O getIfPresent(String key) {
        final O obj = getCache().getIfPresent(key);
        if (obj != null) return obj;
        else {
            final Optional<O> opt = getRepository().read(key);
            return opt.orElse(null);
        }
    }

}
