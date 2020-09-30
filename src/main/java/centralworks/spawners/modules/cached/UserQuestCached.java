package centralworks.spawners.modules.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.SyncRequests;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class UserQuestCached {

    private static UserQuestCached instance;

    public static UserQuestCached getInstance() {
        return instance == null ? instance = new UserQuestCached(null) : instance;
    }

    public UserQuestCached(Consumer<UserQuestCached> consumer) {
        if (consumer != null) consumer.accept(this);
        else {
            var repository = new PlayerQuests().getRepository();
            var config = Main.getInstance().getCacheConfig();
            var path = "PlayerQuests.";
            var removalListener = new RemovalListener<String, PlayerQuests>() {
                @Override
                public void onRemoval(RemovalNotification<String, PlayerQuests> notification) {
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
                    .build(new CacheLoader<String, PlayerQuests>() {
                        @Override
                        public PlayerQuests load(@NotNull String s) {
                            return SyncRequests.supply(repository, s).persist();
                        }
                    });
            repository.findAll().forEach(user -> loadingCache.put(user.getName(), user));
        }
    }

    @Getter
    @Setter
    private LoadingCache<String, PlayerQuests> loadingCache;
}
