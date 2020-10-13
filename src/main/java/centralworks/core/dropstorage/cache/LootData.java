package centralworks.core.dropstorage.cache;

import centralworks.Main;
import centralworks.core.dropstorage.models.Drop;
import centralworks.lib.Cache;
import centralworks.lib.Configuration;
import centralworks.lib.inventory.Item;
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

    public void load() {
        clear();
        final Configuration configuration = Main.getInstance().getDropStorage();
        configuration.section("Drops").forEach(s -> {
            final String path = "Drops." + s + ".";
            final Drop drop = Drop.builder()
                    .keyDrop(s)
                    .unitPrice(configuration.getDouble(path + "unit-sales-value"))
                    .entityType(EntityType.valueOf(configuration.get(path + "mob", false)))
                    .menuItem(new Item(Material.getMaterial(configuration.getInt(path + "drop-item-menu.id")), configuration.getInt(path + "drop-item-menu.amount"), configuration.getInt(path + "drop-item-menu.data").shortValue()).name(configuration.get(path + "drop-item-menu.name", true)).lore(configuration.getList(path + "drop-item-menu.lore", true)))
                    .drop(new ItemStack(configuration.getInt(path + "id"), 1, configuration.getInt(path + "data").shortValue()))
                    .build();
            add(drop);
        });
    }
}
