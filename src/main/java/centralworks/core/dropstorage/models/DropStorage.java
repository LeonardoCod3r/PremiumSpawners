package centralworks.core.dropstorage.models;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.dropstorage.cache.BonusRegistered;
import centralworks.core.dropstorage.cache.LootData;
import centralworks.database.Storable;
import centralworks.database.specifications.BindRepository;
import centralworks.database.specifications.Repository;
import centralworks.repositories.json.FastDropStorageRepository;
import centralworks.repositories.mysql.JpaDropStorageRepository;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class DropStorage extends Storable<DropStorage> implements Serializable {

    @Id
    @Column(length = 16)
    @Getter
    @Setter
    @Expose
    private String owner;
    @Setter
    @Getter
    @Expose
    private Double multiplier = 1.0;
    @Getter
    @Setter
    @Expose
    private boolean autoSell = false;
    @Getter
    @Setter
    @Expose
    private Integer bonus = 0;
    @OneToMany(mappedBy = "dropStorage", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    @Expose
    private List<BoosterPlayer> boostersActive = Lists.newArrayList();
    @OneToMany(mappedBy = "dropStorage", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    @Expose
    private List<DropPlayer> dropPlayers = Lists.newArrayList();
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    @Expose
    private List<String> friends = Lists.newArrayList();

    public DropStorage(OfflinePlayer p) {
        this.owner = p.getName();
    }

    public DropStorage(String owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Repository<DropStorage, String> getRepository() {
        final BindRepository<DropStorage, String> bindRepository = new BindRepository<>(DropStorage.class, JpaDropStorageRepository.require(), FastDropStorageRepository.require());
        return bindRepository.getRelativeRepository();
    }

    @Override
    public Object getEntityIdentifier() {
        return this.owner;
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
                list.add(new DropPlayer(this, drop.getKeyDrop(), 0D));
                setDropPlayers(list);
            }
        });
    }

    public void fixBonus(Player p) {
        setBonus(BonusRegistered.get(p));
    }

    public void applyBoostersDefault() {
        final Double b = Double.valueOf(Main.getInstance().getDropStorage().getList("Boosters.default", false).stream().filter(s -> getOwnerPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1.0").split(":")[1]);
        if (getMultiplier() < b) setMultiplier(b);
    }

    public void sellAll() {
        getDropPlayers().forEach(dropPlayer -> dropPlayer.sell(getOwnerPlayer(), this));
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
        final LoadingCache<String, UserDetails> cache = Caches.getCache(UserDetails.class);
        return cache.getUnchecked(getOwner());
    }

    public boolean isMax() {
        return getAmountAll() >= getUser().getSellLimit();
    }

    public boolean isMax(Double amount) {
        return getAmountAll() + amount > getUser().getSellLimit();
    }

    public Player getOwnerPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public String getCorrectNameOwner() {
        return Bukkit.getOfflinePlayer(owner).getName();
    }

    public Double getAllMultipliers() {
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
        final LoadingCache<String, DropStorage> cache = Caches.getCache(DropStorage.class);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Optional.ofNullable(cache.getIfPresent(getOwner())).ifPresent(storage -> {
            final ArrayList<BoosterPlayer> list = new ArrayList<>(storage.getBoostersActive());
            list.remove(boosterPlayer);
            storage.setBoostersActive(list);
        }), 20L * boosterPlayer.getTime());
    }

}
