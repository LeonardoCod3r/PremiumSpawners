package centralworks.spawners.modules.models;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.specifications.PropertyType;
import centralworks.spawners.commons.database.Storable;
import centralworks.spawners.lib.Serialize;
import centralworks.spawners.modules.models.spawners.Spawner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class UserDetails extends Storable<UserDetails> {

    private String user;
    private List<String> locationsSerialized = Lists.newArrayList();
    private Double buyLimit = 1.0;
    private Double sellLimit = 1.0;

    public UserDetails(OfflinePlayer p){
        this.user = p.getName();
    }

    public UserDetails(String user) {
        this.user = user;
    }

    @Override
    public Properties getProperties() {
        final Properties properties = new Properties();
        properties.put(PropertyType.KEY_NAME.getId(), "user");
        properties.put(PropertyType.KEY_AUTOINCREMENT.getId(), false);
        properties.put(PropertyType.KEY_DATATYPE.getId(), "VARCHAR(16)");
        properties.put(PropertyType.TABLE_NAME.getId(), "users");
        return properties;
    }

    @Override
    public Object getIdentifier() {
        return this.user;
    }

    @Override
    public void setIdentifier(Object object) {
        this.user = object.toString();
    }

    public void getSpawners(Consumer<List<Spawner>> callback) {
        CompletableFuture.supplyAsync(() -> locationsSerialized.stream().map(s -> new Spawner(s).query().persist()).collect(Collectors.toList())).thenAccept(callback);
    }

    public List<Spawner> getSpawners() {
        return locationsSerialized.stream().map(s -> new Spawner(s).query().persist()).collect(Collectors.toList());
    }

    public void fixLimits() {
        if (Bukkit.getPlayer(user) != null) {
            final Double l = Double.valueOf(Main.getLimits().getList("Limits.default", false).stream().filter(s -> getPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1").split(":")[1]);
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
