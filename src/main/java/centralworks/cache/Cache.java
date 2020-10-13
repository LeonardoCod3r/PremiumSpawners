package centralworks.cache;

import centralworks.database.Storable;
import com.google.common.cache.LoadingCache;

@SuppressWarnings("UnstableApiUsage")
public interface Cache<O extends Storable<O>> {

    void create();

    LoadingCache<String, O> getCache();


}
