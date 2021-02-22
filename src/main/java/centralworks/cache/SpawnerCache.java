package centralworks.cache;

import centralworks.Main;
import centralworks.cache.google.AbstractCache;
import centralworks.cache.google.CacheModule;
import centralworks.database.Repository;
import centralworks.repositories.JpaSpawnerRepository;
import centralworks.spawners.models.Spawner;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@CacheModule
public class SpawnerCache extends AbstractCache<Spawner> {

    private LoadingCache<String, Spawner> loadingCache;

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Class<Spawner> getClassRelative() {
        return Spawner.class;
    }

    @Override
    public Repository<Spawner, String> getRepository() {
        return JpaSpawnerRepository.require();
    }

    @Override
    public void create() {
        var nav = Main.getInstance().getCacheConfig().navigate();
        var path = "Spawner.";
        var removalListener = new RemovalListener<String, Spawner>() {
            @Override
            public void onRemoval(RemovalNotification<String, Spawner> notification) {
                final RemovalCause cause = notification.getCause();
                if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause) && notification.getValue() != null) {
                    getRepository().commit(notification.getValue());
                }
            }
        };
        loadingCache = CacheBuilder.newBuilder()
                .maximumSize(nav.getLong(path + "size"))
                .expireAfterWrite(nav.getLong(path + "expired"), TimeUnit.MINUTES)
                .removalListener(removalListener)
                .build(new CacheLoader<String, Spawner>() {

                    @Override
                    public Spawner load(@NotNull String s) {
                        return getRepository().read(s).get();
                    }
                });
        getRepository().findAll().forEach(spawner -> loadingCache.put(spawner.getLocSerialized(), spawner));
    }

    @Override
    public LoadingCache<String, Spawner> getCache() {
        return loadingCache;
    }


}
