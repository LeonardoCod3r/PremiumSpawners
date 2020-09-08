package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.FormatTime;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.menu.settings.BoosterMenuS;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.Settings;
import centralworks.spawners.modules.models.addons.ImpulseType;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerImpulse;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BoosterMenu extends InventoryBuilder {

    public BoosterMenu(Spawner spawner, Player p) {
        super(Main.get(), 3, "§8Gerador");
        clear();
        setCancellable(true);

        final BoosterMenuS menu = MenusSettings.get().getBoosterMenuSettings();
        final Configuration messages = Main.getMessages();
        final Settings se = Settings.get();

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> spawner.query().ifExists(spawner1 -> {
            if (!spawner1.hasPermission(p.getName())) {
                p.closeInventory();
                return;
            }
            new InfoSpawnerMenu(spawner1, p);
        }, exception -> p.closeInventory())));

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)));

        final List<String> lore = Lists.newArrayList();
        if (spawner.hasImpulse()) {
            for (SpawnerImpulse impulse : spawner.getImpulsesOfGeneration()) {
                lore.add("§b" + impulse.getImpulseType().getName() + "/" + new FormatTime(impulse.getFinish() - System.currentTimeMillis()).format());
            }
        } else lore.add("§cNenhum");

        setItem(menu.getBoosters().getItem_slot(), new Item(menu.getBoosters().getAsItem(s -> s)).addLines(lore));

        onClickPlayerInv(e -> spawner.query().ifExists(spawner1 -> {
            if (spawner1.isOwner(p.getName())) {
                try {
                    final ItemStack item = e.getCurrentItem();
                    final NBTItem nbt = new NBTItem(item);
                    final Double multiplier = nbt.getDouble(se.getNBT_TAG_BOOSTER_VALUE());
                    final Integer delay = nbt.getInteger(se.getNBT_TAG_BOOSTER_DELAY());
                    final ImpulseType type = ImpulseType.valueOf(nbt.getString(se.getNBT_TAG_BOOSTER_TYPE()));
                    if (type == ImpulseType.DROPS) return;
                    if (spawner1.hasBoosterActive(type)) {
                        p.sendMessage(messages.getMessage("onlyBooster").replace("{type}", type.name()));
                        return;
                    }
                    final String name = p.getName();
                    if (item.getAmount() == 1) p.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                    else item.setAmount(item.getAmount() - 1);
                    p.sendMessage(messages.getMessage("boosterActivated").replace("{type}", type.name()));
                    new SpawnerImpulse(type, delay, multiplier).go(spawner1, () -> {
                        if (Bukkit.getPlayer(name) != null)
                            Bukkit.getPlayer(name).sendMessage(messages.getMessage("boosterEnd").replace("{type}", type.name()));
                        spawner1.query().commit();
                    });
                } catch (Exception ignored) {
                }
            } else p.sendMessage(messages.getMessage("onlyActiveBooster"));
        }, exception -> p.closeInventory()).commit());

        open(p);
    }
}
