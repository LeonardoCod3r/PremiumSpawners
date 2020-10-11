package centralworks.layouts;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.InventoryBuilder;
import centralworks.lib.Item;
import centralworks.layouts.settings.MenusSettings;
import centralworks.layouts.settings.RankingMenuS;
import centralworks.lib.ItemSettings;
import centralworks.core.spawners.cache.SpawnerRanking;
import centralworks.core.spawners.models.Spawner;
import com.google.common.cache.LoadingCache;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class RankingMenu extends InventoryBuilder {

    public RankingMenu(Spawner spawner, Player p) {
        super(Main.getInstance(), 3, "§8Ranking");
        final RankingMenuS menu = MenusSettings.get().getRankingMenuSettings();
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        clear();
        setCancellable(true);

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) return;
                new InfoSpawnerMenu(spawner1, p);
            });
        }));

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