package centralworks.layouts.menus.spawner;

import centralworks.spawners.models.Spawner;
import centralworks.layouts.settings.InfoSpawnerMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.inventory.Item;
import centralworks.lib.inventory.addons.InventorySpawner;
import org.bukkit.entity.Player;

public class InfoSpawnerMenu extends InventorySpawner {
    
    private static final InfoSpawnerMenuS menu;
    
    static {
        menu = MenusSettings.get().getInfoSpawnerMenuSettings();
    }

    public InfoSpawnerMenu(Spawner spawner, Player p) {
        super(menu.getRows(), menu.getName());
        setTarget(spawner);
        setPlayer(p);
    }

    @Override
    public void load() {
        if (!menu.isToggle()) return;
        clear();
        setCancellable(true);

        setItem(menu.getSpawner_item().getItem_slot(), new Item(menu.getSpawner_item().getAsItem(s -> s
                .replace("{coins}", BalanceFormatter.format(getTarget().getPriceAll()))
                .replace("{owner}", getTarget().getOwner())
                .replace("{entity-type}", getTarget().getEntityName())
                .replace("{stack}", BalanceFormatter.format(getTarget().getAmount()))
                .replace("{location}", "x: " + getTarget().getLocation().getBlockX() + " y: " + getTarget().getLocation().getBlockY() + " z: " + getTarget().getLocation().getBlockZ()))
        ));

        setItem(menu.getBoosters().getItem_slot(), new Item(menu.getBoosters().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new BoosterMenu(spawner1, getPlayer()).load();
            });
        }));

        setItem(menu.getUtils().getItem_slot(), new Item(menu.getUtils().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new UtilitiesMenu(spawner1, getPlayer()).load();
            });
        }));

        setItem(menu.getFriends().getItem_slot(), new Item(menu.getFriends().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.isOwner(getPlayer().getName())) return;
                new FriendsMenu(spawner1, getPlayer(), 1).load();
            });
        }));

        setItem(menu.getTop().getItem_slot(), new Item(menu.getTop().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new RankingMenu(spawner1, getPlayer()).load();
            });
        }));

        open(getPlayer());
    }
}
