package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.BalanceFormatter;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.menu.settings.RankingMenuS;
import centralworks.spawners.modules.models.ItemSettings;
import centralworks.spawners.modules.models.ranking.SpawnerRanking;
import centralworks.spawners.modules.models.spawners.Spawner;
import org.bukkit.entity.Player;

import java.util.List;

public class RankingMenu extends InventoryBuilder {

    public RankingMenu(Spawner spawner, Player p) {
        super(Main.getInstance(), 3, "§8Ranking");
        final RankingMenuS menu = MenusSettings.get().getRankingMenuSettings();
        clear();
        setCancellable(true);

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            new InfoSpawnerMenu(spawner1, p);
        }, exception -> p.closeInventory())));
        final SpawnerRanking ranking = SpawnerRanking.get();
        if (!ranking.isLoaded()) {
            setItem(menu.getUpdating().getItem_slot(), new Item(menu.getUpdating().getAsItem(s -> s)));
            open(p);
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

        open(p);
    }
}
