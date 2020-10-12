package centralworks.layouts;

import centralworks.Main;
import centralworks.core.spawners.cache.TCached;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.enums.TaskType;
import centralworks.layouts.settings.FriendsMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Configuration;
import centralworks.lib.ItemSettings;
import centralworks.lib.inventory.Item;
import centralworks.lib.inventory.addons.InventorySpawner;
import org.bukkit.entity.Player;

import java.util.List;

public class FriendsMenu extends InventorySpawner {

    private final int page;

    public FriendsMenu(Spawner spawner, Player p, int page) {
        super(6, "§8Gerador");
        setTarget(spawner);
        setPlayer(p);
        this.page = page;
    }

    @Override
    public void load() {
        final FriendsMenuS menu = MenusSettings.get().getFriendsMenuSettings();
        final Configuration messages = Main.getInstance().getMessages();
        final TCached cached = TCached.get();
        clear();
        setCancellable(true);

        setItem(menu.getSpawner_item().getItem_slot(), new Item(menu.getSpawner_item().getAsItem(s -> s
                .replace("{coins}", BalanceFormatter.format(getTarget().getPriceAll()))
                .replace("{owner}", getTarget().getOwner())
                .replace("{entity-type}", getTarget().getEntityName())
                .replace("{stack}", BalanceFormatter.format(getTarget().getAmount()))
                .replace("{location}", "x: " + getTarget().getLocation().getBlockX() + " y: " + getTarget().getLocation().getBlockY() + " z: " + getTarget().getLocation().getBlockZ()))
        ));

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.isOwner(getPlayer().getName())) return;
                new InfoSpawnerMenu(spawner1, getPlayer()).load();
            });
        }));

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.isOwner(getPlayer().getName())) return;
                if (!cached.exists(s -> s.getPlayerName().equalsIgnoreCase(getPlayer().getName()))) {
                    cached.add(new TCached.TaskObj(getPlayer().getName(), spawner1.getLocSerialized(), TaskType.ADD_FRIEND));
                    getPlayer().sendMessage(messages.getMessage("taskAdd"));
                }
            });
        }));

        final List<String> friends = getTarget().getFriends();
        final List<Integer> slots = menu.getSlots();
        if (friends.size() > 0) {
            final double pages = Math.ceil(friends.size() / (0D + slots.size()));

            if (page != pages) {
                setItem(menu.getNext_page().getItem_slot(), new Item(menu.getNext_page().getAsItem(s -> s)).onClick(e -> {
                    getPlayer().closeInventory();
                    ifPresent(spawner1 -> {
                        if (!spawner1.isOwner(getPlayer().getName())) return;
                        new FriendsMenu(spawner1, getPlayer(), page + 1).load();
                    });
                }));
            }

            if (page != 1) {
                setItem(menu.getBack_page().getItem_slot(), new Item(menu.getBack_page().getAsItem(s -> s)).onClick(e -> {
                    getPlayer().closeInventory();
                    ifPresent(spawner1 -> {
                        if (!spawner1.isOwner(getPlayer().getName())) return;
                        new FriendsMenu(spawner1, getPlayer(), page - 1).load();
                    });
                }));
            }

            final List<String> list = friends.subList(slots.size() * (page - 1), Math.min(friends.size(), slots.size() * (page + 1)));
            for (int count = 0; count < slots.size() && count < list.size(); count++) {
                final String friend = list.get(count);
                final ItemSettings item = menu.getPlayer();
                setItem(slots.get(count), new Item(item.getAsItem(s -> s))
                        .setSkullOwner(item.getItem_skull_owner().replace("{player}", friend))
                        .name(item.getItem_name().replace("{player}", friend))
                        .onClick(e -> {
                            getPlayer().closeInventory();
                            ifPresent(spawner1 -> {
                                if (!spawner1.isOwner(getPlayer().getName())) return;
                                spawner1.removeFriend(friend);
                                new FriendsMenu(spawner1, getPlayer(), 1).load();
                            });
                        })
                );
            }
        }

        open(getPlayer());
    }
}
