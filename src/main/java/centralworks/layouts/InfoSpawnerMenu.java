package centralworks.layouts;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.InventoryBuilder;
import centralworks.lib.Item;
import centralworks.layouts.settings.InfoSpawnerMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.core.spawners.models.Spawner;
import com.google.common.cache.LoadingCache;
import org.bukkit.entity.Player;

import java.util.Optional;

public class InfoSpawnerMenu extends InventoryBuilder {

    private final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);

    private Optional<Spawner> of(Spawner spawner) {
        return Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized()));
    }

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

        setItem(mainMenuS.getBoosters().getItem_slot(), new Item(mainMenuS.getBoosters().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            of(spawner).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) return;
                new BoosterMenu(spawner1, p);
            });
        }));

        setItem(mainMenuS.getUtils().getItem_slot(), new Item(mainMenuS.getUtils().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            of(spawner).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) return;
                new UtilitiesMenu(spawner1, p);
            });
        }));

        setItem(mainMenuS.getFriends().getItem_slot(), new Item(mainMenuS.getFriends().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            of(spawner).ifPresent(spawner1 -> {
                if (!spawner1.isOwner(p.getName())) return;
                new FriendsMenu(spawner1, p, 1);
            });
        }));

        setItem(mainMenuS.getTop().getItem_slot(), new Item(mainMenuS.getTop().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            of(spawner).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) return;
                new RankingMenu(spawner1, p);
            });
        }));

        open(p);
    }

}
