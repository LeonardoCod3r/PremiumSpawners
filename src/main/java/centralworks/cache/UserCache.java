package centralworks.cache;

import centralworks.Main;
import centralworks.cache.google.AbstractCache;
import centralworks.cache.google.CacheModule;
import centralworks.database.Repository;
import centralworks.models.User;
import centralworks.repositories.JpaUserRepository;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@CacheModule
public class UserCache extends AbstractCache<User> {

    private LoadingCache<String, User> loadingCache;

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<User> getClassRelative() {
        return User.class;
    }

    @Override
    public Repository<User, String> getRepository() {
        return JpaUserRepository.require();
    }

    @Override
    public void create() {
        var nav = Main.getInstance().getCacheConfig().navigate();
        var path = "User.";
        var removalListener = new RemovalListener<String, User>() {
            @Override
            public void onRemoval(RemovalNotification<String, User> notification) {
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
                .build(new CacheLoader<String, User>() {
                    @Override
                    public User load(@NotNull String s) {
                        return getRepository().read(s).get();
                    }
                });
        getRepository().findAll().forEach(user -> loadingCache.put(user.getName(), user));
    }

    @Override
    public LoadingCache<String, User> getCache() {
        return loadingCache;
    }

}
