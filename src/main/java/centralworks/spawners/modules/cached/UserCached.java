package centralworks.spawners.modules.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.SyncRequests;
import centralworks.spawners.modules.models.UserDetails;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class UserCached {

    private static UserCached instance;

    public static UserCached getInstance() {
        return instance == null ? instance = new UserCached(null) : instance;
    }

    public UserCached(Consumer<UserCached> consumer) {
        if (consumer != null) consumer.accept(this);
        else {
            var repository = new UserDetails().getRepository();
            var config = Main.getInstance().getCacheConfig();
            var path = "User.";
            var removalListener = new RemovalListener<String, UserDetails>() {
                @Override
                public void onRemoval(RemovalNotification<String, UserDetails> notification) {
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
                    .build(new CacheLoader<String, UserDetails>() {
                        @Override
                        public UserDetails load(@NotNull String s) {
                            return SyncRequests.supply(repository, s).persist();
                        }
                    });
            repository.findAll().forEach(user -> loadingCache.put(user.getUser(), user));
        }
    }

    @Getter
    @Setter
    private LoadingCache<String, UserDetails> loadingCache;


}
