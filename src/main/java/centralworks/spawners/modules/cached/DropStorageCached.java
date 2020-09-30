package centralworks.spawners.modules.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.SyncRequests;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class DropStorageCached {

    private static DropStorageCached instance;

    public static DropStorageCached getInstance() {
        return instance == null ? instance = new DropStorageCached(null) : instance;
    }

    public DropStorageCached(Consumer<DropStorageCached> consumer) {
        if (consumer != null) consumer.accept(this);
        else {
            var repository = new DropStorage().getRepository();
            var config = Main.getInstance().getCacheConfig();
            var path = "DropStorage.";
            var removalListener = new RemovalListener<String, DropStorage>() {
                @Override
                public void onRemoval(RemovalNotification<String, DropStorage> notification) {
                    final RemovalCause cause = notification.getCause();
                    if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE).contains(cause)) {
                        if (notification.getValue() != null) notification.getValue().query().commit();
                    }
                }
            };
            cache = CacheBuilder.newBuilder()
                    .maximumSize(config.getLong(path + "size"))
                    .expireAfterAccess(config.getLong(path + "expired"), TimeUnit.MINUTES)
                    .removalListener(removalListener)
                    .build(new CacheLoader<String, DropStorage>() {
                        @Override
                        public DropStorage load(@NotNull String s) {
                            return SyncRequests.supply(repository, s).persist();
                        }
                    });
            repository.findAll().forEach(user -> cache.put(user.getOwner(), user));
        }
    }

    @Getter
    @Setter
    private LoadingCache<String, DropStorage> cache;
}
