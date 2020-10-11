package centralworks.layouts;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.lib.Configuration;
import centralworks.lib.InventoryBuilder;
import centralworks.lib.Item;
import centralworks.layouts.settings.MenusSettings;
import centralworks.layouts.settings.UtilitiesMenuS;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerItem;
import centralworks.core.spawners.models.enums.TaskType;
import centralworks.core.spawners.cache.TCached;
import com.google.common.cache.LoadingCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class UtilitiesMenu extends InventoryBuilder {

    public UtilitiesMenu(Spawner spawner, Player p) {
        super(Main.getInstance(), 3, "§8Gerador");
        final UtilitiesMenuS menu = MenusSettings.get().getUtilsMenuSettings();
        final Configuration messages = Main.getInstance().getMessages();
        final TCached trCached = TCached.get();
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);

        clear();
        setCancellable(true);

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)));

        setItem(menu.getRemove().getItem_slot(), new Item(menu.getRemove().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.isOwner(p.getName())) return;
                if (!trCached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
                    trCached.add(new TCached.TaskObj(p.getName(), spawner1.getLocSerialized(), TaskType.REMOVE_SPAWNERS));
                    p.sendMessage(messages.getMessage("taskRemove"));
                }
            });
        }));

        setItem(menu.getGet().getItem_slot(), new Item(menu.getGet().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.isOwner(p.getName())) return;
                new SpawnerItem().parse(spawner).giveItem(p);
                spawner1.destroy(Caches.getCache(UserDetails.class).getUnchecked(p.getName()));
                p.sendMessage(messages.getMessage("spawnerRemoved"));
            });
        }));

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) return;
                new InfoSpawnerMenu(spawner1, p);
            });
        }));

        onClickPlayerInv(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) return;
                if (spawner1.concat(e.getCurrentItem())) {
                    p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                }
            });
        });
        open(p);
    }
}
