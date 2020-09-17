package centralworks.spawners.modules.models.dropsstorage;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.Storable;
import centralworks.spawners.commons.database.specifications.PropertyType;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.dropsstorage.supliers.Drop;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.BonusRegistered;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Data
public class DropStorage extends Storable<DropStorage> {

    private String owner;
    private Double multiplier = 1.0;
    private boolean autoSell = false;
    private Integer bonus = 0;
    private List<BoosterPlayer> boostersActive = Lists.newArrayList();
    private List<DropPlayer> dropPlayers = Lists.newArrayList();
    private List<String> friends = Lists.newArrayList();

    public DropStorage(OfflinePlayer p) {
        this.owner = p.getName();
    }

    public DropStorage(String owner) {
        this.owner = owner;
    }

    @Override
    public Properties getProperties() {
        final Properties properties = new Properties();
        properties.put(PropertyType.KEY_NAME.getId(), "owner");
        properties.put(PropertyType.KEY_AUTOINCREMENT.getId(), false);
        properties.put(PropertyType.KEY_DATATYPE.getId(), "VARCHAR(16)");
        properties.put(PropertyType.TABLE_NAME.getId(), "dropstorage");
        return properties;
    }

    @Override
    public Object getIdentifier() {
        return this.owner;
    }

    @Override
    public void setIdentifier(Object object) {
        this.owner = object.toString();
    }

    public String isAutoSellResult() {
        return isAutoSell() ? "ativado" : "desativado";
    }

    public Double getAmountAll() {
        Double amountAll = 0D;
        for (DropPlayer dropPlayer : getDropPlayers()) {
            amountAll += dropPlayer.getAmount();
        }
        return amountAll;
    }

    public void fixDrops() {
        LootData.get().getList().forEach(drop -> {
            if (getDropPlayers().stream().noneMatch(dropPlayer -> dropPlayer.getKeyDrop().equalsIgnoreCase(drop.getKeyDrop()))) {
                final List<DropPlayer> list = new ArrayList<>(getDropPlayers());
                list.add(new DropPlayer(drop.getKeyDrop(), 0D));
                setDropPlayers(list);
            }
        });
    }

    public void fixBonus(Player p) {
        setBonus(BonusRegistered.get(p));
    }

    public void applyBoostersDefault() {
        final Double b = Double.valueOf(Main.getDropStorage().getList("Boosters.default", false).stream().filter(s -> getOwnerPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1.0").split(":")[1]);
        if (getMultiplier() < b) setMultiplier(b);
    }

    public void sellAll() {
        getDropPlayers().forEach(dropPlayer -> dropPlayer.sell(getOwnerPlayer(), this));
        query().commit();
    }

    public DropPlayer getDropPlayer(String keyDrop) {
        return getDropPlayers().stream().filter(dropPlayer -> dropPlayer.getKeyDrop().equals(keyDrop)).findFirst().get();
    }

    public Double getPriceAll() {
        double priceAll = 0D;
        for (DropPlayer dropPlayer : dropPlayers) {
            final Drop drop = LootData.get().get(dropPlayer.getKeyDrop());
            priceAll += drop.getUnitPrice() * dropPlayer.getAmount();
        }
        return priceAll;
    }

    public Double getPriceWithBonus() {
        return getPriceAll() + (getPriceAll() * getBonus() / 100);
    }

    public UserDetails getUser() {
        return new UserDetails(getOwner()).query().persist();
    }

    public boolean isMax() {
        return getAmountAll() >= new UserDetails(getOwner()).query().persist().getSellLimit();
    }

    public boolean isMax(Double amount) {
        return getAmountAll() + amount > new UserDetails(getOwner()).query().persist().getSellLimit();
    }

    public Player getOwnerPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public String getCorrectNameOwner() {
        return Bukkit.getOfflinePlayer(owner).getName();
    }

    public Double getMultiplier() {
        return multiplier + getMultiplierBoosters();
    }

    public Double getOriginalMultiplier() {
        return multiplier;
    }

    public void addMultiplier(Double multiplier) {
        this.multiplier += multiplier;
    }

    public Double getMultiplierBoosters() {
        Double value = 0D;
        for (BoosterPlayer boosterPlayer : getBoostersActive()) {
            value += boosterPlayer.getMultiplier();
        }
        return value;
    }

    public void addBooster(BoosterPlayer boosterPlayer) {
        getBoostersActive().add(boosterPlayer);
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> query().queue((storage, q) -> {
            final ArrayList<BoosterPlayer> list = new ArrayList<>(storage.getBoostersActive());
            list.remove(boosterPlayer);
            storage.setBoostersActive(list);
            q.commit();
        }), 20L * boosterPlayer.getTime());
    }

}
