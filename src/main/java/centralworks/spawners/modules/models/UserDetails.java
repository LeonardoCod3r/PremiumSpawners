package centralworks.spawners.modules.models;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.repositories.fast.FastUserRepository;
import centralworks.spawners.lib.database.specifications.BindRepository;
import centralworks.spawners.lib.database.specifications.Repository;
import centralworks.spawners.lib.database.repositories.jpa.JpaUserRepository;
import centralworks.spawners.lib.database.Storable;
import centralworks.spawners.lib.Serialize;
import centralworks.spawners.modules.models.spawners.Spawner;
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
public class UserDetails extends Storable<UserDetails> implements Serializable {

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
    private Double sellLimit = 1.0;
    @SuppressWarnings("unchecked")
    @Override
    public Repository<UserDetails, String> getRepository() {
        final BindRepository<UserDetails, String> bindRepository = new BindRepository<>(UserDetails.class, JpaUserRepository.require(), FastUserRepository.require());
        return bindRepository.getRelativeRepository();
    }

    public UserDetails(OfflinePlayer p) {
        this.user = p.getName();
    }

    public UserDetails(String user) {
        this.user = user;
    }

    @Override
    public Object getEntityIdentifier() {
        return this.user;
    }

    public void getSpawners(Consumer<List<Spawner>> callback) {
        CompletableFuture.supplyAsync(() -> locationsSerialized.stream().map(s -> new Spawner(s).query().persist()).collect(Collectors.toList())).thenAccept(callback);
    }

    public List<Spawner> getSpawners() {
        return locationsSerialized.stream().map(s -> new Spawner(s).query().persist()).collect(Collectors.toList());
    }

    public void fixLimits() {
        if (Bukkit.getPlayer(user) != null) {
            final Double l = Double.valueOf(Main.getInstance().getDropStorage().getList("Limits.default", false).stream().filter(s -> getPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1").split(":")[1]);
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
        locationsSerialized.add(new Serialize<Location, String>(location).getResult());
    }

    public Spawner getSpawner(Location location) {
        return new Spawner(locationsSerialized.stream().filter(location1 -> location1.equals(new Serialize<Location, String>(location).getResult())).findFirst().get()).query().persist();
    }

    public boolean exists(Location location) {
        return locationsSerialized.stream().map(s -> new Serialize<String, Location>(s).getResult()).anyMatch(location1 -> location1.equals(location));
    }

    public void deleteSpawnerLocation(Location location) {
        locationsSerialized.remove(new Serialize<Location, String>(location).getResult());
    }
}
