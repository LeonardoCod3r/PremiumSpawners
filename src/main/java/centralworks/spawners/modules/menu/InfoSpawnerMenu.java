package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.BalanceFormatter;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.menu.settings.InfoSpawnerMenuS;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.spawners.Spawner;
import org.bukkit.entity.Player;

public class InfoSpawnerMenu extends InventoryBuilder {

    public InfoSpawnerMenu(Spawner spawner, Player p) {
        super(Main.getInstance(), 3, "§8Gerador");
        final InfoSpawnerMenuS mainMenuS = MenusSettings.get().getInfoSpawnerMenuSettings();
        clear();
        setCancellable(true);

        setItem(mainMenuS.getSpawner_item().getItem_slot(), new Item(mainMenuS.getSpawner_item().getAsItem(s -> s
                .replace("{coins}", BalanceFormatter.format(spawner.getPriceAll()))
                .replace("{owner}", spawner.getOwner())
                .replace("{entity-type}", spawner.getEntityName())
                .replace("{stack}", BalanceFormatter.format(spawner.getAmount()))
                .replace("{location}", "x: " + spawner.getLocation().getBlockX() + " y: " + spawner.getLocation().getBlockY() + " z: " + spawner.getLocation().getBlockZ()))
        ));

        setItem(mainMenuS.getBoosters().getItem_slot(), new Item(mainMenuS.getBoosters().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            new BoosterMenu(spawner1, p);
        }, exception -> p.closeInventory())));

        setItem(mainMenuS.getUtils().getItem_slot(), new Item(mainMenuS.getUtils().getAsItem(s -> s)
        ).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            new UtilitiesMenu(spawner1, p);
        }, exception -> p.closeInventory())));

        setItem(mainMenuS.getFriends().getItem_slot(), new Item(mainMenuS.getFriends().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.isOwner(p.getName())) {
                p.closeInventory();
                return;
            }
            new FriendsMenu(spawner1, p, 1);
        }, exception -> p.closeInventory())));

        setItem(mainMenuS.getTop().getItem_slot(), new Item(mainMenuS.getTop().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            new RankingMenu(spawner1, p);
        }, exception -> p.closeInventory())));

        open(p);
    }

}
