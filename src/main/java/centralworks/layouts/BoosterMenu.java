package centralworks.layouts;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.lib.Configuration;
import centralworks.lib.FormatTime;
import centralworks.lib.InventoryBuilder;
import centralworks.lib.Item;
import centralworks.layouts.settings.BoosterMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.core.spawners.Settings;
import centralworks.core.commons.models.enums.ImpulseType;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class BoosterMenu extends InventoryBuilder {

    public BoosterMenu(Spawner spawner, Player p) {
        super(Main.getInstance(), 3, "§8Gerador");
        clear();
        setCancellable(true);

        final BoosterMenuS menu = MenusSettings.get().getBoosterMenuSettings();
        final Configuration messages = Main.getInstance().getMessages();
        final Settings se = Settings.get();
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            p.closeInventory();
            Optional.ofNullable(cache.asMap().get(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(p.getName())) {
                    p.closeInventory();
                    return;
                }
                new InfoSpawnerMenu(spawner1, p);
            });
        }));

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)));

        final List<String> lore = Lists.newArrayList();
        if (spawner.hasImpulse()) {
            for (SpawnerImpulse impulse : spawner.getImpulsesOfGeneration()) {
                lore.add("§b" + impulse.getImpulseType().getName() + "/" + new FormatTime(impulse.getFinish() - System.currentTimeMillis()).format());
            }
        } else lore.add("§cNenhum");

        setItem(menu.getBoosters().getItem_slot(), new Item(menu.getBoosters().getAsItem(s -> s)).addLines(lore));

        onClickPlayerInv(e -> {
            Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
                if (spawner1.isOwner(p.getName())) {
                    try {
                        final ItemStack item = e.getCurrentItem();
                        final NBTItem nbt = new NBTItem(item);
                        final Double multiplier = nbt.getDouble(se.getNBT_TAG_BOOSTER_VALUE());
                        final Integer delay = nbt.getInteger(se.getNBT_TAG_BOOSTER_DELAY());
                        final ImpulseType type = ImpulseType.valueOf(nbt.getString(se.getNBT_TAG_BOOSTER_TYPE()));
                        if (type == ImpulseType.DROPS) return;
                        p.closeInventory();
                        if (spawner1.hasBoosterActive(type)) {
                            p.sendMessage(messages.getMessage("onlyBooster").replace("{type}", type.name()));
                            return;
                        }
                        final String name = p.getName();
                        if (item.getAmount() == 1) p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                        else item.setAmount(item.getAmount() - 1);
                        p.sendMessage(messages.getMessage("boosterActivated").replace("{type}", type.name()));
                        new SpawnerImpulse(type, delay, multiplier).in(spawner1, () -> {
                            if (Bukkit.getPlayer(name) != null)
                                Bukkit.getPlayer(name).sendMessage(messages.getMessage("boosterEnd").replace("{type}", type.name()));
                        });
                    } catch (Exception ignored) {
                    }
                } else {
                    p.sendMessage(messages.getMessage("onlyActiveBooster"));
                    p.closeInventory();
                }
            });
        });

        open(p);
    }
}
