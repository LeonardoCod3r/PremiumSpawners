package centralworks.cache.google;

import centralworks.database.Repository;
import com.google.common.cache.LoadingCache;

@SuppressWarnings("UnstableApiUsage")
public interface Cache<O> {

    void create();

    LoadingCache<String, O> getCache();

    O getIfPresent(String key);

    Repository<O, String> getRepository();

    int getPriority();


}
