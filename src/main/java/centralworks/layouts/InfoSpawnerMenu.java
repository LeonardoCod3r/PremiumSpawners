package centralworks.layouts;

import centralworks.core.spawners.models.Spawner;
import centralworks.layouts.settings.InfoSpawnerMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.inventory.Item;
import centralworks.lib.inventory.addons.InventorySpawner;
import org.bukkit.entity.Player;

public class InfoSpawnerMenu extends InventorySpawner {

    public InfoSpawnerMenu(Spawner spawner, Player p) {
        super(3, "§8Gerador");
        setTarget(spawner);
        setPlayer(p);
    }

    @Override
    public void load() {
        final InfoSpawnerMenuS mainMenuS = MenusSettings.get().getInfoSpawnerMenuSettings();
        clear();
        setCancellable(true);

        setItem(mainMenuS.getSpawner_item().getItem_slot(), new Item(mainMenuS.getSpawner_item().getAsItem(s -> s
                .replace("{coins}", BalanceFormatter.format(getTarget().getPriceAll()))
                .replace("{owner}", getTarget().getOwner())
                .replace("{entity-type}", getTarget().getEntityName())
                .replace("{stack}", BalanceFormatter.format(getTarget().getAmount()))
                .replace("{location}", "x: " + getTarget().getLocation().getBlockX() + " y: " + getTarget().getLocation().getBlockY() + " z: " + getTarget().getLocation().getBlockZ()))
        ));

        setItem(mainMenuS.getBoosters().getItem_slot(), new Item(mainMenuS.getBoosters().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new BoosterMenu(spawner1, getPlayer()).load();
            });
        }));

        setItem(mainMenuS.getUtils().getItem_slot(), new Item(mainMenuS.getUtils().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new UtilitiesMenu(spawner1, getPlayer()).load();
            });
        }));

        setItem(mainMenuS.getFriends().getItem_slot(), new Item(mainMenuS.getFriends().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.isOwner(getPlayer().getName())) return;
                new FriendsMenu(spawner1, getPlayer(), 1).load();
            });
        }));

        setItem(mainMenuS.getTop().getItem_slot(), new Item(mainMenuS.getTop().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new RankingMenu(spawner1, getPlayer());
            });
        }));

        open(getPlayer());
    }
}
