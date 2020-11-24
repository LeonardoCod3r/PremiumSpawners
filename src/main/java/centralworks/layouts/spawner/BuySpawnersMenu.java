package centralworks.layouts.spawner;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.commons.models.User;
import centralworks.core.spawners.cache.SICached;
import centralworks.core.spawners.cache.TCached;
import centralworks.core.spawners.enums.TaskType;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.FormatTime;
import centralworks.lib.Settings;
import centralworks.lib.enums.EntityName;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuySpawnersMenu extends InventoryMaker {

    @SneakyThrows
    public BuySpawnersMenu(Player p) {
        super(6, "§8Comprar Geradores");
        clear();
        setCancellable(true);
        final LoadingCache<String, User> cache = Caches.getCache(User.class);
        final Double buyLimit = cache.getIfPresent(p.getName()).getBuyLimit();
        setItem(4, new Item(Material.getMaterial(397), 1, (short) 3).setSkullOwner(p.getName()).name("§eInformações: ").lore("§fLimite de compra: §7" + BalanceFormatter.format(buyLimit)));

        final ArrayList<Integer> slots = Lists.newArrayList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);

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
        return new Item(SICached.get().get(item -> item.getEntityType() == entityType).getSpawnerItem().getItem())
                .lore("§fPreço: §2R$§7" + BalanceFormatter.format(price), "", "§7Clique para comprar geradores de " + EntityName.valueOf(entityType).getName() + ".")
                .onClick(event -> {
                    p.sendMessage(nav.getMessage("talkAmountToBuy"));
                    p.closeInventory();
                    TCached.get().add(new TCached.TaskObj(p.getName(), entityType.name(), TaskType.BUY_SPAWNERS));
                });
    }

    public Item comingSoon(Date date) {
        return new Item(Material.SKULL_ITEM, 1, (short) 3).setSkullUrl("http://textures.minecraft.net/texture/3ed1aba73f639f4bc42bd48196c715197be2712c3b962c97ebf9e9ed8efa025").name("§c§lEm breve!").lore("§7Esse gerador ainda não foi liberado.").addLines(date == null ? null : Lists.newArrayList("§7Será liberado em " + new FormatTime(date.getTime() - System.currentTimeMillis()).format()) + ".");
    }

}
