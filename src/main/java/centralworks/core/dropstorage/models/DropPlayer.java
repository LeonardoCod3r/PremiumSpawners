package centralworks.core.dropstorage.models;

import centralworks.core.dropstorage.cache.LootData;
import centralworks.hooks.EconomyContext;
import centralworks.lib.enums.PluginSystemType;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.entity.Player;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class DropPlayer {

    @Id
    @GeneratedValue
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Long id;
    @Getter(AccessLevel.PRIVATE)
    @Setter
    @ManyToOne
    @Deprecated
    private DropStorage dropStorage;
    @Getter
    @Setter
    @Expose
    private String keyDrop;
    @Getter
    @Setter
    @Expose
    private Double amount;

    public DropPlayer(DropStorage dropStorage, String keyDrop, Double amount) {
        this.dropStorage = dropStorage;
        this.keyDrop = keyDrop;
        this.amount = amount;
    }

    public void sell(Player p, DropStorage storage) {
        final Drop drop = LootData.get().get(getKeyDrop());
        final double value = drop.getUnitPrice() * getAmount();
        final EconomyContext.Economy economy = EconomyContext.getContext(PluginSystemType.DROPSTORAGE).getEconomy();
        ;
        economy.addMoney(p.getName(), value + (value * storage.getBonus() / 100));
        setAmount(0D);
    }

    public Double getPrice(DropStorage storage) {
        final Drop drop = LootData.get().get(getKeyDrop());
        final double value = drop.getUnitPrice() * getAmount();
        return value + (value * storage.getBonus() / 100);
    }

    public void addDropAmount(Double amount) {
        this.amount += amount;
    }

    public void removeDropAmount(Double amount) {
        this.amount -= amount;
    }
}
