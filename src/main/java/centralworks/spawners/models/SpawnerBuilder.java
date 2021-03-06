package centralworks.spawners.models;


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
        final SpawnerItem item = new SpawnerItem(itemStack);
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
