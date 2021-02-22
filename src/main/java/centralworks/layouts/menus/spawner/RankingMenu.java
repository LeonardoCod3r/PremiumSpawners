package centralworks.layouts.menus.spawner;

import centralworks.cache.simple.SpawnerRanking;
import centralworks.layouts.settings.MenusSettings;
import centralworks.layouts.settings.RankingMenuS;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.ItemSettings;
import centralworks.lib.inventory.Item;
import centralworks.lib.inventory.addons.InventorySpawner;
import centralworks.spawners.models.Spawner;
import org.bukkit.entity.Player;

import java.util.List;

public class RankingMenu extends InventorySpawner {

    private static final RankingMenuS menu;

    static {
        menu = MenusSettings.get().getRankingMenuSettings();
    }

    public RankingMenu(Spawner spawner, Player p) {
        super(menu.getRows(), menu.getName());
        setTarget(spawner);
        setPlayer(p);
    }

    @Override
    public void load() {
        if (!menu.isToggle()) return;
        final SpawnerRanking ranking = SpawnerRanking.get();
        clear();
        setCancellable(true);

        if (getTarget() != null) {
            setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
                getPlayer().closeInventory();
                ifPresent(spawner1 -> {
                    if (!spawner1.hasPermission(getPlayer().getName())) return;
                    new InfoSpawnerMenu(spawner1, getPlayer()).load();
                });
            }));
        }

        if (!ranking.isLoaded()) {
            setItem(menu.getUpdating().getItem_slot(), new Item(menu.getUpdating().getAsItem(s -> s)));
            open(getPlayer());
            return;
        }

        final List<SpawnerRanking.Supplier> suppliers = ranking.getSuppliers();
        final List<Integer> slots = menu.getSlots();

        if (suppliers.size() != 0) {
            for (int count = 0; count < slots.size(); count++) {
                if (suppliers.size() == count) break;
                final SpawnerRanking.Supplier supplier = suppliers.get(count);
                final ItemSettings item = menu.getPlayer();
                final int position = count + 1;
                setItem(slots.get(count), new Item(item.getAsItem(s -> s.replace("{position}", "" + position).replace("{price}", BalanceFormatter.format(supplier.getPriceAll()))))
                        .setSkullOwner(item.getItem_skull_owner().replace("{player}", supplier.getUser()))
                        .name(item.getItem_name().replace("{player}", supplier.getUser()))
                );
            }
        }

        open(getPlayer());
    }
}
