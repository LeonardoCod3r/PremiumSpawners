package centralworks.models;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.cache.simple.BonusRegistered;
import centralworks.market.models.Market;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.*;
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
public class ProductStorage implements Serializable {

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
    @OneToMany(mappedBy = "productStorage", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    @Expose
    private List<BoosterPlayer> boostersActive = Lists.newArrayList();
    @OneToMany(mappedBy = "productStorage", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    @Expose
    private List<UserProduct> userProducts = Lists.newArrayList();
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    @Expose
    private List<String> friends = Lists.newArrayList();

    public ProductStorage(OfflinePlayer p) {
        this.owner = p.getName();
    }

    public ProductStorage(String owner) {
        this.owner = owner;
    }

    public String isAutoSellResult() {
        return isAutoSell() ? "ativado" : "desativado";
    }

    public Double getAmountAll() {
        Double amountAll = 0D;
        for (UserProduct userProduct : getUserProducts()) {
            amountAll += userProduct.getAmount();
        }
        return amountAll;
    }

    public void fixDrops() {
        Market.getInstance().getProducts().forEach(product -> {
            if (getUserProducts().stream().noneMatch(userProduct -> userProduct.getProductKey().equalsIgnoreCase(product.getId()))) {
                final List<UserProduct> list = new ArrayList<>(getUserProducts());
                list.add(new UserProduct(this, product.getId(), 0D));
                setUserProducts(list);
            }
        });
    }

    public void fixBonus(Player p) {
        setBonus(BonusRegistered.get(p));
    }

    public void applyBoostersDefault() {
        final Double b = Double.valueOf(Main.getInstance().getDropStorage().navigate().getList("Boosters.default").stream().filter(s -> getOwnerPlayer().hasPermission(s.split(":")[0])).findFirst().orElse("0:1.0").split(":")[1]);
        if (getMultiplier() < b) setMultiplier(b);
    }

    public void sellAll() {
        getUserProducts().forEach(userProduct -> userProduct.sell(getOwnerPlayer(), this));
    }

    public UserProduct getDropPlayer(String keyDrop) {
        return getUserProducts().stream().filter(userProduct -> userProduct.getProductKey().equals(keyDrop)).findFirst().get();
    }

    public Double getPriceAll() {
        var priceAll = 0D;
        for (UserProduct userProduct : userProducts) {
            priceAll += userProduct.getPrice(this);
        }
        return priceAll;
    }

    public User getUser() {
        final LoadingCache<String, User> cache = Caches.getCache(User.class);
        return cache.getIfPresent(getOwner());
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
        final LoadingCache<String, ProductStorage> cache = Caches.getCache(ProductStorage.class);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Optional.ofNullable(cache.getIfPresent(getOwner())).ifPresent(storage -> {
            final ArrayList<BoosterPlayer> list = new ArrayList<>(storage.getBoostersActive());
            list.remove(boosterPlayer);
            storage.setBoostersActive(list);
        }), 20L * boosterPlayer.getTime());
    }

}
