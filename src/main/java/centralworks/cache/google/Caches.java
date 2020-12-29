package centralworks.cache.google;

import centralworks.Main;
import centralworks.models.User;
import centralworks.models.ProductStorage;
import centralworks.quests.models.PlayerQuests;
import centralworks.spawners.models.Spawner;
import centralworks.database.Storable;
import centralworks.database.SyncRequests;
import centralworks.database.specifications.Repository;
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

    private Caches() {
        throw new IllegalStateException("Utility class");
    }

    static {
        caches = Maps.newHashMap();

        caches.put(Spawner.class, new AbstractCache<Spawner>() {

            private LoadingCache<String, Spawner> loadingCache;

            @Override
            public Repository<Spawner, String> getRepository() {
                return new Spawner().getRepository();
            }

            @Override
            public void create() {
                var repository = new Spawner().getRepository();
                var nav = Main.getInstance().getCacheConfig().navigate();
                var path = "Spawner.";
                var removalListener = new RemovalListener<String, Spawner>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, Spawner> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause) && notification.getValue() != null) {
                            notification.getValue().query().commit();
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

        caches.put(User.class, new AbstractCache<User>() {

            private LoadingCache<String, User> loadingCache;

            @Override
            public Repository<User, String> getRepository() {
                return new User().getRepository();
            }

            @Override
            public void create() {
                var repository = new User().getRepository();
                var nav = Main.getInstance().getCacheConfig().navigate();
                var path = "User.";
                var removalListener = new RemovalListener<String, User>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, User> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause) && notification.getValue() != null) {
                            notification.getValue().query().commit();
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
                                return SyncRequests.supply(repository, s).persist();
                            }
                        });
                repository.findAll().forEach(user -> loadingCache.put(user.getName(), user));
            }

            @Override
            public LoadingCache<String, User> getCache() {
                return loadingCache;
            }
        });

        caches.put(ProductStorage.class, new AbstractCache<ProductStorage>() {

            private LoadingCache<String, ProductStorage> loadingCache;

            @Override
            public Repository<ProductStorage, String> getRepository() {
                return new ProductStorage().getRepository();
            }

            @Override
            public void create() {
                var repository = new ProductStorage().getRepository();
                var nav = Main.getInstance().getCacheConfig().navigate();
                var path = "DropStorage.";
                var removalListener = new RemovalListener<String, ProductStorage>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, ProductStorage> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause) && notification.getValue() != null) {
                            notification.getValue().query().commit();
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
                                return SyncRequests.supply(repository, s).persist();
                            }
                        });
                repository.findAll().forEach(user -> loadingCache.put(user.getOwner(), user));
            }

            @Override
            public LoadingCache<String, ProductStorage> getCache() {
                return loadingCache;
            }
        });

        caches.put(PlayerQuests.class, new AbstractCache<PlayerQuests>() {

            private LoadingCache<String, PlayerQuests> loadingCache;

            @Override
            public Repository<PlayerQuests, String> getRepository() {
                return new PlayerQuests().getRepository();
            }

            @Override
            public void create() {
                var repository = new PlayerQuests().getRepository();
                var nav = Main.getInstance().getCacheConfig().navigate();
                var path = "PlayerQuests.";
                var removalListener = new RemovalListener<String, PlayerQuests>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, PlayerQuests> notification) {
                        final RemovalCause cause = notification.getCause();
                        if (Lists.newArrayList(RemovalCause.EXPIRED, RemovalCause.SIZE, RemovalCause.EXPLICIT).contains(cause) && notification.getValue() != null) {
                            notification.getValue().query().commit();
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
