package centralworks.lib.inventory.addons;

import centralworks.cache.Caches;
import centralworks.core.spawners.models.Spawner;
import centralworks.lib.inventory.InventoryMaker;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class InventorySpawner extends InventoryMaker {

    @Getter
    @Setter
    private Spawner target;
    @Getter
    @Setter
    private Player player;
    @Getter
    private final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);

    public InventorySpawner() {
    }

    public InventorySpawner(Integer rows, String name) {
        super(rows, name);
    }

    public Spawner takeACurrentInstance() {
        return cache.getIfPresent(target.getLocSerialized());
    }

    public Spawner ifPresent(Consumer<Spawner> consumer) {
        final Spawner spawner = takeACurrentInstance();
        if (consumer!=null && Optional.ofNullable(spawner).isPresent()) consumer.accept(spawner);
        return spawner;
    }

    public abstract void load();






}
