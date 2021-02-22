package centralworks.cache.google;

import com.google.common.cache.*;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import lombok.Getter;

import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
public class Caches {

    @Getter
    private static final HashMap<Class<?>, AbstractCache<?>> caches;

    private Caches() {
        throw new IllegalStateException("Utility class");
    }

    static {
        caches = Maps.newHashMap();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith("centralworks.cache.")) {
                    final Class<?> clazz = info.load();
                    if (clazz.isAnnotationPresent(CacheModule.class)) {
                        final AbstractCache<?> ac = (AbstractCache<?>) clazz.newInstance();
                        caches.put(ac.getClassRelative(), ac);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        caches.values().forEach(Cache::create);
    }

    @SuppressWarnings("unchecked")
    public static <T> LoadingCache<String, T> getCache(Class<T> clazz) {
        return (LoadingCache<String, T>) caches.get(clazz).getCache();
    }

}
