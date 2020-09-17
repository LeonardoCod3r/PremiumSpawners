package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.menu.settings.UtilitiesMenuS;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerItem;
import centralworks.spawners.modules.models.spawners.TaskType;
import centralworks.spawners.modules.models.spawners.cached.TCached;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UtilitiesMenu extends InventoryBuilder {

    public UtilitiesMenu(Spawner spawner, Player p) {
        super(Main.get(), 3, "§8Gerador");
        final UtilitiesMenuS menu = MenusSettings.get().getUtilsMenuSettings();
        final Configuration messages = Main.getMessages();
        final TCached trCached = TCached.get();
        clear();
        setCancellable(true);

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)));

        setItem(menu.getRemove().getItem_slot(), new Item(menu.getRemove().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.isOwner(p.getName())) {
                p.closeInventory();
                return;
            }
            if (!trCached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
                trCached.add(new TCached.TaskObj(p.getName(), spawner1.getLocSerialized(), TaskType.REMOVE_SPAWNERS));
                p.sendMessage(messages.getMessage("taskRemove"));
                p.closeInventory();
            }
        }, exception -> p.closeInventory())));

        setItem(menu.getGet().getItem_slot(), new Item(menu.getGet().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.isOwner(p.getName())) {
                p.closeInventory();
                return;
            }
            new SpawnerItem().parse(spawner).giveItem(p);
            spawner1.destroy(new UserDetails(p).query().persist());
            p.sendMessage(messages.getMessage("spawnerRemoved"));
            p.closeInventory();
        }, exception -> p.closeInventory())));

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            new InfoSpawnerMenu(spawner1, p);
        }, exception -> p.closeInventory())));

        onClickPlayerInv(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            if (spawner1.concat(e.getCurrentItem())) {
                p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                spawner1.query().commit();
            }
        }, exception -> p.closeInventory()));
        open(p);
    }
}
