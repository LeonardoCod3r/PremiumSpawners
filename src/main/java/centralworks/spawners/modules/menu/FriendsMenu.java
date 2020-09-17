package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.menu.settings.FriendsMenuS;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.ItemSettings;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.TaskType;
import centralworks.spawners.modules.models.spawners.cached.TCached;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class FriendsMenu extends InventoryBuilder {

    public FriendsMenu(Spawner spawner, Player p, int page) {
        super(Main.get(), 6, "§8Gerador");
        final FriendsMenuS menu = MenusSettings.get().getFriendsMenuSettings();
        final Configuration messages = Main.getMessages();
        final TCached cached = TCached.get();
        clear();
        setCancellable(true);

        setItem(menu.getSpawner_item().getItem_slot(), new Item(menu.getSpawner_item().getAsItem(s -> s
                .replace("{coins}", FormatBalance.format(spawner.getPriceAll()))
                .replace("{owner}", spawner.getOwner())
                .replace("{entity-type}", spawner.getEntityName())
                .replace("{stack}", FormatBalance.format(spawner.getAmount()))
                .replace("{location}", "x: " + spawner.getLocation().getBlockX() + " y: " + spawner.getLocation().getBlockY() + " z: " + spawner.getLocation().getBlockZ()))
        ));

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.isOwner(p.getName())) {
                p.closeInventory();
                return;
            }
            new InfoSpawnerMenu(spawner1, p);
        }, exception -> p.closeInventory())));

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            p.closeInventory();
            if (!spawner1.isOwner(p.getName())) return;
            if (!cached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
                cached.add(new TCached.TaskObj(p.getName(), spawner1.getLocSerialized(), TaskType.ADD_FRIEND));
                p.sendMessage(messages.getMessage("taskAdd"));
                p.closeInventory();
            }
        }, exception -> p.closeInventory())));

        final LinkedList<String> friends = spawner.getFriends();
        final List<Integer> slots = menu.getSlots();
        if (friends.size() > 0) {
            final double pages = Math.ceil(friends.size() / (0D + slots.size()));

            if (page != pages) {
                setItem(menu.getNext_page().getItem_slot(), new Item(menu.getNext_page().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
                    if (!spawner1.isOwner(p.getName())) {
                        p.closeInventory();
                        return;
                    }
                    new FriendsMenu(spawner1, p, page + 1);
                }, exception -> p.closeInventory())));
            }

            if (page != 1) {
                setItem(menu.getBack_page().getItem_slot(), new Item(menu.getBack_page().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
                    if (!spawner1.isOwner(p.getName())) {
                        p.closeInventory();
                        return;
                    }
                    new FriendsMenu(spawner1, p, page - 1);
                }, exception -> p.closeInventory())));
            }

            final List<String> list = friends.subList(slots.size() * (page - 1), Math.min(friends.size(), slots.size() * (page + 1)));
            for (int count = 0; count < slots.size() && count < list.size(); count++) {
                final String friend = list.get(count);
                final ItemSettings item = menu.getPlayer();
                setItem(slots.get(count), new Item(item.getAsItem(s -> s))
                        .setSkullOwner(item.getItem_skull_owner().replace("{player}", friend))
                        .name(item.getItem_name().replace("{player}", friend))
                        .onClick(e -> spawner.query().ifExists(spawner1 -> {
                            if (!spawner1.isOwner(p.getName())) {
                                p.closeInventory();
                                return;
                            }
                            spawner1.removeFriend(friend);
                            spawner1.query().commit();
                            new FriendsMenu(spawner1, p, 1);
                        }, exception -> p.closeInventory()))
                );
            }

        }

        open(p);
    }
}
