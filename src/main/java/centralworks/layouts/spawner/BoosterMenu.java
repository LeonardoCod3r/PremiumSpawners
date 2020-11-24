package centralworks.layouts.spawner;

import centralworks.Main;
import centralworks.core.commons.models.enums.ImpulseType;
import centralworks.core.spawners.Identifiers;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
import centralworks.layouts.settings.BoosterMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.FormatTime;
import centralworks.lib.Settings;
import centralworks.lib.inventory.Item;
import centralworks.lib.inventory.addons.InventorySpawner;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BoosterMenu extends InventorySpawner {

    public BoosterMenu(Spawner spawner, Player p) {
        super(3, "§8Gerador");
        setTarget(spawner);
        setPlayer(p);
    }

    @Override
    public void load() {
        clear();
        setCancellable(true);

        final BoosterMenuS menu = MenusSettings.get().getBoosterMenuSettings();
        final Settings.Navigate nav = Main.getInstance().getMessages().navigate();

        setItem(menu.getBack().getItem_slot(), new Item(menu.getBack().getAsItem(s -> s)).onClick(e -> {
            getPlayer().closeInventory();
            ifPresent(spawner1 -> {
                if (!spawner1.hasPermission(getPlayer().getName())) return;
                new InfoSpawnerMenu(spawner1, getPlayer()).load();
            });
        }));

        setItem(menu.getAdd().getItem_slot(), new Item(menu.getAdd().getAsItem(s -> s)));

        final List<String> lore = Lists.newArrayList();
        if (getTarget().hasImpulse()) {
            for (SpawnerImpulse impulse : getTarget().getImpulsesOfGeneration()) {
                lore.add("§b" + impulse.getImpulseType().getName() + "/" + new FormatTime(impulse.getFinish() - System.currentTimeMillis()).format());
            }
        } else lore.add("§cNenhum");

        setItem(menu.getBoosters().getItem_slot(), new Item(menu.getBoosters().getAsItem(s -> s)).addLines(lore));

        onClick(e -> ifPresent(spawner1 -> {
            if (spawner1.isOwner(getPlayer().getName())) {
                try {
                    final ItemStack item = e.getCurrentItem();
                    final NBTItem nbt = new NBTItem(item);
                    final Double multiplier = nbt.getDouble(Identifiers.NBT_TAG_BOOSTER_VALUE);
                    final Integer delay = nbt.getInteger(Identifiers.NBT_TAG_BOOSTER_DELAY);
                    final ImpulseType type = ImpulseType.valueOf(nbt.getString(Identifiers.NBT_TAG_BOOSTER_TYPE));
                    if (type == ImpulseType.DROPS) return;
                    getPlayer().closeInventory();
                    if (spawner1.hasBoosterActive(type)) {
                        getPlayer().sendMessage(nav.getMessage("onlyBooster").replace("{type}", type.name()));
                        return;
                    }
                    final String name = getPlayer().getName();
                    if (item.getAmount() == 1)
                        getPlayer().getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
                    else item.setAmount(item.getAmount() - 1);
                    getPlayer().sendMessage(nav.getMessage("boosterActivated").replace("{type}", type.name()));
                    new SpawnerImpulse(type, delay, multiplier).in(spawner1, () -> {
                        if (Bukkit.getPlayer(name) != null)
                            Bukkit.getPlayer(name).sendMessage(nav.getMessage("boosterEnd").replace("{type}", type.name()));
                    });
                } catch (Exception ignored) {
                }
            } else {
                getPlayer().sendMessage(nav.getMessage("onlyActiveBooster"));
                getPlayer().closeInventory();
            }
        }));

        open(getPlayer());
    }
}
