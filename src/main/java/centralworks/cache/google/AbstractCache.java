package centralworks.cache.google;

import java.util.Optional;

public abstract class AbstractCache<O> implements Cache<O> {

    public O getIfPresent(String key) {
        final O obj = getCache().getIfPresent(key);
        if (obj != null) return obj;
        else {
            final Optional<O> opt = getRepository().read(key);
            return opt.orElse(null);
        }
    }

    public abstract Class<O> getClassRelative();

}
