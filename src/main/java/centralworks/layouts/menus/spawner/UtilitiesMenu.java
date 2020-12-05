package centralworks.layouts.menus.spawner;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.User;
import centralworks.cache.simple.TCached;
import centralworks.spawners.TaskType;
import centralworks.events.SpawnerRemoveEvent;
import centralworks.spawners.models.Spawner;
import centralworks.spawners.models.SpawnerItem;
import centralworks.layouts.settings.MenusSettings;
import centralworks.layouts.settings.UtilitiesMenuS;
import centralworks.lib.Settings;
import centralworks.lib.inventory.Item;
import centralworks.lib.inventory.addons.InventorySpawner;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UtilitiesMenu extends InventorySpawner {

    private static final UtilitiesMenuS menu;

    static {
        menu = MenusSettings.get().getUtilsMenuSettings();
    }

    public UtilitiesMenu(Spawner spawner, Player p) {
        super(menu.getRows(), menu.getName());
        setTarget(spawner);
        setPlayer(p);
    }

    @Override
    public void load() {
        if (!menu.isToggle()) return;
        final Settings.Navigate nav = Main.getInstance().getMessages().navigate();
        final TCached trCached = TCached.get();

        clear();
        setCancellable(true);

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)));

        setItem(menu.getRemove().getItem_slot(), new Item(menu.getRemove().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.isOwner(getPlayer().getName())) return;
                if (!trCached.exists(s -> s.getPlayerName().equalsIgnoreCase(getPlayer().getName()))) {
                    trCached.add(new TCached.TaskObj(getPlayer().getName(), spawner1.getLocSerialized(), TaskType.REMOVE_SPAWNERS));
                    getPlayer().sendMessage(nav.getMessage("taskRemove"));
                }
            });
        }));

        setItem(menu.getGet().getItem_slot(), new Item(menu.getGet().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.isOwner(getPlayer().getName())) return;
                final SpawnerItem spawnerItem = new SpawnerItem().parse(getTarget());
                final SpawnerRemoveEvent event = new SpawnerRemoveEvent(getPlayer(), spawner1, spawnerItem, true);
                if (event.isCancelled()) return;
                spawnerItem.giveItem(getPlayer());
                spawner1.destroy(Caches.getCache(User.class).getIfPresent(getPlayer().getName()));
                getPlayer().sendMessage(nav.getMessage("spawnerRemoved"));
            });
        }));

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new InfoSpawnerMenu(spawner1, getPlayer()).load();
            });
        }));

        onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                if (spawner1.concat(getPlayer(), e.getCurrentItem())) {
                    getPlayer().getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                }
            });
        });

        open(getPlayer());
    }

}
