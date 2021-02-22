package centralworks.models;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.spawners.models.Spawner;
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
public class User implements Serializable {

    @Id
    @Column(length = 16)
    @Getter
    @Setter
    @Expose
    private String name;
    @ElementCollection
    @JoinColumn(name = "name")
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
        this.name = p.getName();
    }

    public User(String name) {
        this.name = name;
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
        if (Bukkit.getPlayer(name) != null) {
            var settings = Main.getInstance().getDropStorage().navigate();
            final Double l = Double.valueOf(settings.getList("Limits.default").stream().filter(s -> getPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1").split(":")[1]);
            if (getBuyLimit() < l) setBuyLimit(l);
            if (getSellLimit() < l) setSellLimit(l);
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(name);
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
        locationsSerialized.add(Main.getGson().toJson(location));
    }

    public Spawner getSpawner(Location location) {
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        return cache.getIfPresent(locationsSerialized.stream().filter(location1 -> location1.equals(Main.getGson().toJson(location))).findFirst().orElse(""));
    }

    public boolean exists(Location location) {
        return locationsSerialized.stream().map(loc -> Main.getGson().fromJson(loc, Location.class)).anyMatch(location1 -> location1.equals(location));
    }

    public void deleteSpawnerLocation(Location location) {
        locationsSerialized.remove(Main.getGson().toJson(location));
    }
}
