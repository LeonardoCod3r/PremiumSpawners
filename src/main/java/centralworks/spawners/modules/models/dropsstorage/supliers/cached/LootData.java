package centralworks.spawners.modules.models.dropsstorage.supliers.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Cache;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.models.dropsstorage.supliers.Drop;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class LootData extends Cache<Drop> {

    private static LootData me;

    public static LootData get() {
        return me == null ? me = new LootData() : me;
    }

    public Drop get(String key) {
        return getList().stream().filter(drop -> drop.getKeyDrop().equalsIgnoreCase(key)).findFirst().orElseGet(() -> getList().get(0));
    }

    public void load(){
        clear();
        final Configuration configuration = Main.getConfiguration();
        configuration.section("Drops").forEach(s -> {
            final String path = "Drops." + s + ".";
            final Drop drop = Drop.builder()
                    .keyDrop(s)
                    .unitPrice(configuration.getDouble(path+ "unit-sales-value"))
                    .entityType(EntityType.valueOf(configuration.get(path + "mob", false)))
                    .menuItem(new Item(Material.getMaterial(configuration.getInt(path + "drop-item-menu.id")), configuration.getInt(path + "drop-item-menu.amount"), configuration.getInt(path + "drop-item-menu.data").shortValue()).name(configuration.get(path + "drop-item-menu.name", true)).lore(configuration.getList(path + "drop-item-menu.lore", true)))
                    .drop(new ItemStack(configuration.getInt(path + "id"), 1, configuration.getInt(path + "data").shortValue()))
                    .build();
            add(drop);
        });
    }
}
