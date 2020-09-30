package centralworks.spawners.modules.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.SyncRequests;
import centralworks.spawners.modules.models.spawners.Spawner;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class SpawnerCached {

    private static SpawnerCached instance;

    public static SpawnerCached getInstance() {
        return instance == null ? instance = new SpawnerCached(null) : instance;
    }

    private SpawnerCached(Consumer<SpawnerCached> consumer) {
        if (consumer != null) consumer.accept(this);
        else {
            var repository = new Spawner().getRepository();
            var config = Main.getInstance().getCacheConfig();
            var path = "Spawner.";
            var removalListener = new RemovalListener<String, Spawner>() {
                @Override
                public void onRemoval(RemovalNotification<String, Spawner> notification) {
                    final RemovalCause cause = notification.getCause();
                    if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE).contains(cause)) {
                        if (notification.getValue() != null) notification.getValue().query().commit();
                    }
                }
            };
            loadingCache = CacheBuilder.newBuilder()
                    .maximumSize(config.getLong(path + "size"))
                    .expireAfterAccess(config.getLong(path + "expired"), TimeUnit.MINUTES)
                    .removalListener(removalListener)
                    .build(new CacheLoader<String, Spawner>() {
                        @Override
                        public Spawner load(@NotNull String s) {
                            return SyncRequests.supply(repository, s).persist();
                        }
                    });
            repository.findAll().forEach(spawner -> loadingCache.put(spawner.getLocSerialized(), spawner));
        }
    }

    @Getter
    @Setter
    private LoadingCache<String, Spawner> loadingCache;


}
