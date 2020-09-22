package centralworks.spawners.modules.models.dropsstorage;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.commons.database.repositories.DropStorageRepository;
import centralworks.spawners.modules.models.dropsstorage.supliers.Drop;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import com.google.gson.annotations.Expose;
import lombok.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import javax.persistence.*;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class DropPlayer {

    @Expose(serialize = false, deserialize = false)
    @ManyToOne
    @Getter
    @Setter
    private DropStorage dropStorage;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    @Expose
    private Long id;
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
