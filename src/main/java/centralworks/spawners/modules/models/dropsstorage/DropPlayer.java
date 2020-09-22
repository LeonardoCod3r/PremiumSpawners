package centralworks.spawners.modules.models.dropsstorage;

import centralworks.spawners.Main;
import centralworks.spawners.modules.models.dropsstorage.supliers.Drop;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import com.google.gson.annotations.Expose;
import lombok.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class DropPlayer {

    @Getter
    @Setter
    @ManyToOne
    @Expose(serialize = false, deserialize = false)
    private DropStorage dropStorage;
    @Getter
    @Setter
    private String keyDrop;
    @Getter
    @Setter
    private Double amount;

    public void sell(Player p, DropStorage storage) {
        final Drop drop = LootData.get().get(getKeyDrop());
        final Economy economy = Main.getEconomy();
        final double value = drop.getUnitPrice() * getAmount();
        economy.depositPlayer(p, value + (value * storage.getBonus() / 100));
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
