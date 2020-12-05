package centralworks.cache.simple;

import centralworks.Main;
import centralworks.models.Drop;
import centralworks.lib.ListCache;
import centralworks.lib.Settings;
import centralworks.lib.inventory.Item;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class LootData extends ListCache<Drop> {

    private static LootData me;

    public static LootData get() {
        return me == null ? me = new LootData() : me;
    }

    public Drop get(String key) {
        return getList().stream().filter(drop -> drop.getKeyDrop().equalsIgnoreCase(key)).findFirst().orElseGet(() -> getList().get(0));
    }

    public void load() {
        clear();
        final Settings.Navigate nav = Main.getInstance().getDropStorage().navigate();
        nav.section("Drops").forEach(s -> {
            final String path = "Drops." + s + ".";
            final Drop drop = Drop.builder()
                    .keyDrop(s)
                    .unitPrice(nav.getDouble(path + "unit-sales-value"))
                    .entityType(EntityType.valueOf(nav.getString(path + "mob")))
                    .menuItem(new Item(Material.getMaterial(nav.getInt(path + "drop-item-menu.id")), nav.getInt(path + "drop-item-menu.amount"), nav.getInt(path + "drop-item-menu.data").shortValue()).name(nav.getColorfulString(path + "drop-item-menu.name")).lore(nav.getColorfulList(path + "drop-item-menu.lore")))
                    .drop(new ItemStack(nav.getInt(path + "id"), 1, nav.getInt(path + "data").shortValue()))
                    .build();
            add(drop);
        });
    }
}
