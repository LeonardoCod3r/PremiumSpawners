package centralworks.cache;

import centralworks.Main;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.dropstorage.models.DropStorage;
import centralworks.core.quests.models.PlayerQuests;
import centralworks.core.spawners.models.Spawner;
import centralworks.database.Storable;
import centralworks.database.SyncRequests;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.var;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class Caches {

    @Getter
    private static final HashMap<Class<?>, AbstractCache<?>> caches;

    static {
        caches = Maps.newHashMap();

        caches.put(Spawner.class, new AbstractCache<Spawner>() {

            private LoadingCache<String, Spawner> loadingCache;

            @Override
            public void create() {
                var repository = new Spawner().getRepository();
                var config = Main.getInstance().getCacheConfig();
                var path = "Spawner.";
                var removalListener = new RemovalListener<String, Spawner>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, Spawner> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause)) {
                            if (notification.getValue() != null) notification.getValue().query().commit();
                        }
                    }
                };
                loadingCache = CacheBuilder.newBuilder()
                        .maximumSize(config.getLong(path + "size"))
                        .expireAfterWrite(config.getLong(path + "expired"), TimeUnit.MINUTES)
                        .removalListener(removalListener)
                        .build(new CacheLoader<String, Spawner>() {

                            @Override
                            public Spawner load(@NotNull String s) {
                                return SyncRequests.supply(repository, s).persist();
                            }
                        });
                repository.findAll().forEach(spawner -> loadingCache.put(spawner.getLocSerialized(), spawner));
            }

            @Override
            public LoadingCache<String, Spawner> getCache() {
                return loadingCache;
            }
        });

        caches.put(UserDetails.class, new AbstractCache<UserDetails>() {

            private LoadingCache<String, UserDetails> loadingCache;

            @Override
            public void create() {
                var repository = new UserDetails().getRepository();
                var config = Main.getInstance().getCacheConfig();
                var path = "User.";
                var removalListener = new RemovalListener<String, UserDetails>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, UserDetails> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause)) {
                            if (notification.getValue() != null) notification.getValue().query().commit();
                        }
                    }
                };
                loadingCache = CacheBuilder.newBuilder()
                        .maximumSize(config.getLong(path + "size"))
                        .expireAfterWrite(config.getLong(path + "expired"), TimeUnit.MINUTES)
                        .removalListener(removalListener)
                        .build(new CacheLoader<String, UserDetails>() {
                            @Override
                            public UserDetails load(@NotNull String s) {
                                return SyncRequests.supply(repository, s).persist();
                            }
                        });
                repository.findAll().forEach(user -> loadingCache.put(user.getUser(), user));
            }

            @Override
            public LoadingCache<String, UserDetails> getCache() {
                return loadingCache;
            }
        });

        caches.put(DropStorage.class, new AbstractCache<DropStorage>() {

            private LoadingCache<String, DropStorage> loadingCache;

            @Override
            public void create() {
                var repository = new DropStorage().getRepository();
                var config = Main.getInstance().getCacheConfig();
                var path = "DropStorage.";
                var removalListener = new RemovalListener<String, DropStorage>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, DropStorage> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause)) {
                            if (notification.getValue() != null) notification.getValue().query().commit();
                        }
                    }
                };
                loadingCache = CacheBuilder.newBuilder()
                        .maximumSize(config.getLong(path + "size"))
                        .expireAfterWrite(config.getLong(path + "expired"), TimeUnit.MINUTES)
                        .removalListener(removalListener)
                        .build(new CacheLoader<String, DropStorage>() {
                            @Override
                            public DropStorage load(@NotNull String s) {
                                return SyncRequests.supply(repository, s).persist();
                            }
                        });
                repository.findAll().forEach(user -> loadingCache.put(user.getOwner(), user));
            }

            @Override
            public LoadingCache<String, DropStorage> getCache() {
                return loadingCache;
            }
        });

        caches.put(PlayerQuests.class, new AbstractCache<PlayerQuests>() {

            private LoadingCache<String, PlayerQuests> loadingCache;

            @Override
            public void create() {
                var repository = new PlayerQuests().getRepository();
                var config = Main.getInstance().getCacheConfig();
                var path = "PlayerQuests.";
                var removalListener = new RemovalListener<String, PlayerQuests>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, PlayerQuests> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause)) {
                            if (notification.getValue() != null) notification.getValue().query().commit();
                        }
                    }
                };
                loadingCache = CacheBuilder.newBuilder()
                        .maximumSize(config.getLong(path + "size"))
                        .expireAfterWrite(config.getLong(path + "expired"), TimeUnit.MINUTES)
                        .removalListener(removalListener)
                        .build(new CacheLoader<String, PlayerQuests>() {
                            @Override
                            public PlayerQuests load(@NotNull String s) {
                                return SyncRequests.supply(repository, s).persist();
                            }
                        });
                repository.findAll().forEach(user -> loadingCache.put(user.getName(), user));
            }

            @Override
            public LoadingCache<String, PlayerQuests> getCache() {
                return loadingCache;
            }
        });

        caches.values().forEach(Cache::create);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Storable<T>> LoadingCache<String, T> getCache(Class<T> clazz) {
        return (LoadingCache<String, T>) caches.get(clazz).getCache();
    }

}
