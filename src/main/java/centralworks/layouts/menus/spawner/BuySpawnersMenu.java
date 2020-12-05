package centralworks.layouts.menus.spawner;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.User;
import centralworks.cache.simple.SICached;
import centralworks.cache.simple.TCached;
import centralworks.spawners.TaskType;
import centralworks.layouts.settings.BuySpawnersMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.FormatTime;
import centralworks.lib.ItemSettings;
import centralworks.lib.Settings;
import centralworks.lib.enums.EntityName;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BuySpawnersMenu extends InventoryMaker {

    private static final BuySpawnersMenuS menu;

    static {
        menu = MenusSettings.get().getBuySpawnersMenuSettings();
    }


    @SneakyThrows
    public BuySpawnersMenu(Player p) {
        super(6, "§8Comprar Geradores");
        clear();
        setCancellable(true);
        final LoadingCache<String, User> cache = Caches.getCache(User.class);
        final Double buyLimit = cache.getIfPresent(p.getName()).getBuyLimit();
        final ItemSettings info = menu.getInfo().clone();
        final String owner = info.getItem_skull_owner();
        info.setItem_skull_owner(owner.replace("{player}", p.getName()));
        setItem(info.getItem_slot(), new Item(info.getAsItem(s -> s.replace("{limit}", BalanceFormatter.format(buyLimit)))));

        final List<Integer> slots = menu.getSlots();

        final List<String> list = Lists.newArrayList(Main.getInstance().getSpawners().navigate().section("List"));
        for (int i = 0; i < slots.size(); i++) {
            if (list.size() >= (i + 1)) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss-dd/MM/yyyy");
                final String s = list.get(i);
                final Settings.Navigate nav = Main.getInstance().getSpawners().navigate();
                final boolean value = nav.getBoolean("List." + s + ".toggle");
                final String s1 = nav.getString("List." + s + ".in");
                if (value && (s1.equals("") || dateFormat.parse(s1).getTime() < System.currentTimeMillis()))
                    setItem(slots.get(i), getSpawnerItem(p, EntityType.valueOf(s)));
                else setItem(slots.get(i), comingSoon(dateFormat.parse(s1)));
            } else setItem(slots.get(i), comingSoon(null));
        }
        open(p);
    }

    public Item getSpawnerItem(Player p, EntityType entityType) {
        final Settings.Navigate nav = Main.getInstance().getMessages().navigate();
        final Double price = Main.getInstance().getSpawners().navigate().getDouble("List." + entityType.name() + ".price");
        List<String> lore = menu.getSpawnerItem().getItem_lore();
        lore = lore.stream()
                .map(s -> s.replace("{price}", BalanceFormatter.format(price).replace("{mob}", EntityName.valueOf(entityType).getName())))
                .collect(Collectors.toList());
        return new Item(SICached.get().get(item -> item.getEntityType() == entityType).getSpawnerItem().getItem())
                .lore(lore)
                .onClick(event -> {
                    p.sendMessage(nav.getMessage("talkAmountToBuy"));
                    p.closeInventory();
                    TCached.get().add(new TCached.TaskObj(p.getName(), entityType.name(), TaskType.BUY_SPAWNERS));
                });
    }

    public Item comingSoon(Date date) {
        return new Item(menu.getComingSoon().getAsItem(s ->
                s.replace("{date}", date == null ? "invalid" : new FormatTime(date.getTime() - System.currentTimeMillis()).format())
        ));
    }

}
