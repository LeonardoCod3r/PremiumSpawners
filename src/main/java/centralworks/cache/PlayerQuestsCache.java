package centralworks.cache;

import centralworks.Main;
import centralworks.cache.google.AbstractCache;
import centralworks.cache.google.CacheModule;
import centralworks.database.Repository;
import centralworks.quests.models.PlayerQuests;
import centralworks.repositories.JpaUserQuestsRepository;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@CacheModule
public class PlayerQuestsCache extends AbstractCache<PlayerQuests> {

    @Override
    public Class<PlayerQuests> getClassRelative() {
        return PlayerQuests.class;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    private LoadingCache<String, PlayerQuests> loadingCache;

    @Override
    public Repository<PlayerQuests, String> getRepository() {
        return JpaUserQuestsRepository.require();
    }

    @Override
    public void create() {
        var nav = Main.getInstance().getCacheConfig().navigate();
        var path = "PlayerQuests.";
        var removalListener = new RemovalListener<String, PlayerQuests>() {
            @Override
            public void onRemoval(RemovalNotification<String, PlayerQuests> notification) {
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
                .build(new CacheLoader<String, PlayerQuests>() {
                    @Override
                    public PlayerQuests load(@NotNull String s) {
                        return getRepository().read(s).get();
                    }
                });
        getRepository().findAll().forEach(user -> loadingCache.put(user.getName(), user));
    }

    @Override
    public LoadingCache<String, PlayerQuests> getCache() {
        return loadingCache;
    }
}
