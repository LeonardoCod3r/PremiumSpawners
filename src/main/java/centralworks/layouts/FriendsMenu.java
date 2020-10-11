package centralworks.layouts;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.spawners.cache.TCached;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.enums.TaskType;
import centralworks.layouts.settings.FriendsMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.*;
import com.google.common.cache.LoadingCache;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class FriendsMenu extends InventoryBuilder {

    public FriendsMenu(Spawner spawner, Player p, int page) {
        super(Main.getInstance(), 6, "§8Gerador");
        final FriendsMenuS menu = MenusSettings.get().getFriendsMenuSettings();
        final Configuration messages = Main.getInstance().getMessages();
        final TCached cached = TCached.get();
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        clear();
        setCancellable(true);

        setItem(menu.getSpawner_item().getItem_slot(), new Item(menu.getSpawner_item().getAsItem(s -> s
                .replace("{coins}", BalanceFormatter.format(spawner.getPriceAll()))
                .replace("{owner}", spawner.getOwner())
                .replace("{entity-type}", spawner.getEntityName())
                .replace("{stack}", BalanceFormatter.format(spawner.getAmount()))
                .replace("{location}", "x: " + spawner.getLocation().getBlockX() + " y: " + spawner.getLocation().getBlockY() + " z: " + spawner.getLocation().getBlockZ()))
        ));

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.isOwner(p.getName())) return;
                new InfoSpawnerMenu(spawner1, p);
            });
        }));

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.isOwner(p.getName())) return;
                if (!cached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
                    cached.add(new TCached.TaskObj(p.getName(), spawner1.getLocSerialized(), TaskType.ADD_FRIEND));
                    p.sendMessage(messages.getMessage("taskAdd"));
                }
            });
        }));

        final List<String> friends = spawner.getFriends();
        final List<Integer> slots = menu.getSlots();
        if (friends.size() > 0) {
            final double pages = Math.ceil(friends.size() / (0D + slots.size()));

            if (page != pages) {
                setItem(menu.getNext_page().getItem_slot(), new Item(menu.getNext_page().getAsItem(s -> s)).onClick(e -> {
                    p.closeInventory();
                    Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                        if (!spawner1.isOwner(p.getName())) return;
                        new FriendsMenu(spawner1, p, page + 1);
                    });
                }));
            }

            if (page != 1) {
                setItem(menu.getBack_page().getItem_slot(), new Item(menu.getBack_page().getAsItem(s -> s)).onClick(e -> {
                    p.closeInventory();
                    Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                        if (!spawner1.isOwner(p.getName())) return;
                        new FriendsMenu(spawner1, p, page - 1);
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
                            p.closeInventory();
                            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                                if (!spawner1.isOwner(p.getName())) return;
                                spawner1.removeFriend(friend);
                                new FriendsMenu(spawner1, p, 1);
                            });
                        })
                );
            }
        }

        open(p);
    }
}