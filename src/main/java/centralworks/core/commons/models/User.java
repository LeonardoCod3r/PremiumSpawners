package centralworks.core.commons.models;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.spawners.models.Spawner;
import centralworks.database.Storable;
import centralworks.database.specifications.BindRepository;
import centralworks.database.specifications.Repository;
import centralworks.lib.LocationUtils;
import centralworks.repositories.json.FastUserRepository;
import centralworks.repositories.mysql.JpaUserRepository;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class User extends Storable<User> implements Serializable {

    @Id
    @Column(length = 16)
    @Getter
    @Setter
    @Expose
    private String user;
    @ElementCollection
    @JoinColumn(name = "user")
    @Getter
    @Setter
    @Expose
    private List<String> locationsSerialized = Lists.newArrayList();
    @Getter
    @Setter
    @Expose
    private Double buyLimit = 1.0;
    @Getter
    @Setter
    @Expose
    private Long registeredIn = System.currentTimeMillis();
    @Getter
    @Setter
    @Expose
    private Double sellLimit = 1.0;

    public User(OfflinePlayer p) {
        this.user = p.getName();
    }

    public User(String user) {
        this.user = user;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Repository<User, String> getRepository() {
        final BindRepository<User, String> bindRepository = new BindRepository<>(User.class, JpaUserRepository.require(), FastUserRepository.require());
        return bindRepository.getRelativeRepository();
    }

    @Override
    public Object getEntityIdentifier() {
        return this.user;
    }

    public void getSpawners(Consumer<List<Spawner>> callback) {
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        CompletableFuture.supplyAsync(() -> locationsSerialized.stream().map(cache::getIfPresent).collect(Collectors.toList())).thenAccept(callback);
    }

    public List<Spawner> getSpawners() {
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        return locationsSerialized.stream().map(cache::getIfPresent).collect(Collectors.toList());
    }

    public void fixLimits() {
        if (Bukkit.getPlayer(user) != null) {
            var settings = Main.getInstance().getDropStorage().navigate();
            final Double l = Double.valueOf(settings.getList("Limits.default").stream().filter(s -> getPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1").split(":")[1]);
            if (getBuyLimit() < l) setBuyLimit(l);
            if (getSellLimit() < l) setSellLimit(l);
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(user);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(user);
    }

    public boolean isOnline() {
        return getOfflinePlayer().isOnline();
    }

    public void addBuyLimit(Double value) {
        this.buyLimit += value;
    }

    public void removeBuyLimit(Double value) {
        this.buyLimit -= value;
    }

    public void addSellLimit(Double value) {
        this.sellLimit += value;
    }

    public void removeSellLimit(Double value) {
        this.sellLimit -= value;
    }

    public void addSpawnerLocation(Location location) {
        locationsSerialized.add(LocationUtils.locToString(location));
    }

    public Spawner getSpawner(Location location) {
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        return cache.getIfPresent(locationsSerialized.stream().filter(location1 -> location1.equals(LocationUtils.locToString(location))).findFirst().get());
    }

    public boolean exists(Location location) {
        return locationsSerialized.stream().map(LocationUtils::stringToLoc).anyMatch(location1 -> location1.equals(location));
    }

    public void deleteSpawnerLocation(Location location) {
        locationsSerialized.remove(LocationUtils.locToString(location));
    }
}
