package centralworks.cache;

import centralworks.Main;
import centralworks.cache.google.AbstractCache;
import centralworks.cache.google.CacheModule;
import centralworks.database.Repository;
import centralworks.models.ProductStorage;
import centralworks.repositories.JpaDropStorageRepository;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@CacheModule
public class ProductStorageCache extends AbstractCache<ProductStorage> {

    @Override
    public Class<ProductStorage> getClassRelative() {
        return ProductStorage.class;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    private LoadingCache<String, ProductStorage> loadingCache;

    @Override
    public Repository<ProductStorage, String> getRepository() {
        return JpaDropStorageRepository.require();
    }

    @Override
    public void create() {
        var nav = Main.getInstance().getCacheConfig().navigate();
        var path = "DropStorage.";
        var removalListener = new RemovalListener<String, ProductStorage>() {
            @Override
            public void onRemoval(RemovalNotification<String, ProductStorage> notification) {
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
                .build(new CacheLoader<String, ProductStorage>() {
                    @Override
                    public ProductStorage load(@NotNull String s) {
                        return getRepository().read(s).get();
                    }
                });
        getRepository().findAll().forEach(user -> loadingCache.put(user.getOwner(), user));
    }

    @Override
    public LoadingCache<String, ProductStorage> getCache() {
        return loadingCache;
    }
}
