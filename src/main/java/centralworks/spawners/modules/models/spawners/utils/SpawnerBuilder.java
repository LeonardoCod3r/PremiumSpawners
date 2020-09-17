package centralworks.spawners.modules.models.spawners.utils;


import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Data
@RequiredArgsConstructor
public class SpawnerBuilder {

    private String owner;
    private Location location;
    private Spawner resultBuild;

    public SpawnerBuilder(Location location, String owner) {
        this.location = location;
        this.owner = owner;
    }

    public Spawner build(ItemStack itemStack) {
        final Spawner spawner = new Spawner();
        final SpawnerItem item = new SpawnerItem().parse(itemStack);
        spawner.setLocation(location);
        spawner.setAmount(item.getAmountSpawners() * item.getAmountItem());
        spawner.setEntityType(item.getEntityType());
        spawner.setOwner(getOwner());
        return this.resultBuild = spawner;
    }

    public Spawner build(SpawnerItem item) {
        final Spawner spawner = new Spawner();
        spawner.setLocation(location);
        spawner.setAmount(item.getAmountSpawners() * item.getAmountItem());
        spawner.setEntityType(item.getEntityType());
        spawner.setOwner(getOwner());
        return this.resultBuild = spawner;
    }


}
