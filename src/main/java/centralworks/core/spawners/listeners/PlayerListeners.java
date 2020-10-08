package centralworks.core.spawners.listeners;

import centralworks.Main;
import centralworks.core.commons.cache.LimitCached;
import centralworks.core.commons.models.Limit;
import centralworks.core.commons.models.enums.ImpulseType;
import centralworks.core.commons.models.enums.LimitType;
import centralworks.hooks.EconomyContext;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Configuration;
import centralworks.lib.enums.PluginSystemType;
import centralworks.core.spawners.events.BoosterActiveEvent;
import centralworks.core.spawners.Settings;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
import centralworks.core.spawners.models.SpawnerItem;
import centralworks.core.spawners.models.enums.TaskType;
import centralworks.core.spawners.cache.TCached;
import com.google.inject.Inject;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListeners implements Listener {

    private final Main plugin;

    public PlayerListeners() {
        this.plugin = Main.getInstance();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final ItemStack item = e.getItem();
        final Block block = e.getClickedBlock();
        final Settings se = Settings.get();
        final Configuration messages = plugin.getMessages();
        if (item == null) return;
        try {
            final NBTItem nbt = new NBTItem(item);
            final Double multiplier = nbt.getDouble(se.getNBT_TAG_BOOSTER_VALUE());
            final Integer delay = nbt.getInteger(se.getNBT_TAG_BOOSTER_DELAY());
            final ImpulseType type = ImpulseType.valueOf(nbt.getString(se.getNBT_TAG_BOOSTER_TYPE()));
            e.setCancelled(true);
            if (type == ImpulseType.DROPS) return;
            new Spawner(block.getLocation()).query().ifExists(spawner -> {
                if (!spawner.isOwner(p.getName())) {
                    p.sendMessage(messages.getMessage("onlyActiveBooster"));
                    return;
                }
                if (spawner.hasBoosterActive(type)) {
                    p.sendMessage(messages.getMessage("onlyBooster").replace("{type}", type.name()));
                    return;
                }
                final SpawnerImpulse spawnerImpulse = new SpawnerImpulse(type, delay, multiplier);
                final BoosterActiveEvent event = new BoosterActiveEvent(p, spawnerImpulse, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                final String name = p.getName();
                if (item.getAmount() == 1) p.setItemInHand(new ItemStack(Material.AIR));
                else item.setAmount(item.getAmount() - 1);
                p.sendMessage(messages.getMessage("boosterActivated").replace("{type}", type.name()));
                spawnerImpulse.go(spawner, () -> {
                    if (Bukkit.getPlayer(name) != null)
                        Bukkit.getPlayer(name).sendMessage(messages.getMessage("boosterEnd").replace("{type}", type.name()));
                    spawner.query().commit();
                });
            });
        } catch (Exception ignored) {
            if (plugin.limitSystemIsActive()) {
                for (Limit limit : LimitCached.get().getList()) {
                    if (!item.isSimilar(limit.getItemStack().getAsItem(s -> s))) continue;
                    e.setCancelled(true);
                    final LimitType type = limit.getLimitType();
                    if (!limit.hasPermission(p)) {
                        p.sendMessage(messages.getMessage("permissionErrorLimit").replace("{type}", type.getName()));
                        return;
                    }
                    final UserDetails userDetails = new UserDetails(p).query().persist();
                    final Double max = plugin.getDropStorage().getDouble("Limits.max");
                    Double adder;
                    Double v;
                    if (type == LimitType.BUY) {
                        if (userDetails.getBuyLimit() >= max) {
                            p.sendMessage(messages.getMessage("limitMax").replace("{type}", type.getName()));
                            return;
                        }
                        if (userDetails.getBuyLimit() + limit.getValue() > max)
                            adder = limit.getValue() - (userDetails.getBuyLimit() + limit.getValue() - max);
                        else adder = limit.getValue();
                        userDetails.addBuyLimit(adder);
                        v = userDetails.getBuyLimit();
                    } else return;
                    userDetails.query().commit();
                    if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                    else p.setItemInHand(new ItemStack(Material.AIR));
                    p.sendMessage(messages.getMessage("limitUsed").replace("{type}", type.getName()).replace("{limit-value}", BalanceFormatter.format(adder)).replace("{player-limit-value}", BalanceFormatter.format(v)));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onTalk(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        final TCached trCached = TCached.get();
        if (trCached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
            e.setCancelled(true);
            final String msg = e.getMessage();
            final Configuration messages = plugin.getMessages();
            final TCached.TaskObj obj = trCached.get(taskObj -> taskObj.getPlayerName().equalsIgnoreCase(p.getName()));
            if (msg.equalsIgnoreCase("cancelar")) {
                p.sendMessage(messages.getMessage("cancel"));
                trCached.remove(obj);
                return;
            }
            if (obj.getTaskType() == TaskType.REMOVE_SPAWNERS) {
                new Spawner(obj.getValue()).query().ifExists(spawner -> {
                    if (!spawner.isOwner(p.getName())) {
                        trCached.remove(obj);
                        return;
                    }
                    try {
                        final double v = Math.ceil(Double.parseDouble(msg));
                        if (v < 1.0) {
                            p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                            return;
                        }
                        if (v > spawner.getAmount()) {
                            p.sendMessage(messages.getMessage("insufficientSpawners"));
                            return;
                        }
                        trCached.remove(obj);
                        if (v == spawner.getAmount()) {
                            new SpawnerItem().parse(spawner).giveItem(p);
                            spawner.destroy(new UserDetails(p).query().persist());
                            p.sendMessage(messages.getMessage("spawnerRemoved"));
                            return;
                        }
                        final SpawnerItem spawnerItem = new SpawnerItem().parse(spawner);
                        spawnerItem.setAmountSpawners(v);
                        spawnerItem.giveItem(p);
                        spawner.removeStack(v);
                        spawner.updateHologram();
                        p.sendMessage(messages.getMessage("spawnerRemoved2").replace("{amount}", BalanceFormatter.format(v)));
                        spawner.query().commit();
                    } catch (Exception ex) {
                        p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                    }
                }, exception -> trCached.remove(obj));
            } else if (obj.getTaskType() == TaskType.BUY_SPAWNERS) {
                final EntityType entityType = EntityType.valueOf(obj.getValue());
                final Double price = plugin.getSpawners().getDouble("List." + entityType.name() + ".price");
                try {
                    final Double value = Math.ceil(Double.parseDouble(msg));
                    final UserDetails user = new UserDetails(p).query().persist();
                    if (value < 1 || value.isNaN()) {
                        p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                        return;
                    }
                    if (plugin.limitSystemIsActive() && user.getBuyLimit() < value) {
                        p.sendMessage(messages.getMessage("insufficientLimit").replace("{amount}", BalanceFormatter.format(user.getBuyLimit())));
                        return;
                    }
                    final double priceAll = value * price;
                    final EconomyContext.Economy economy = EconomyContext.getContext(PluginSystemType.SPAWNERS_BUY).getEconomy();
                    if (economy.getBalance(p.getName()) < priceAll) {
                        p.sendMessage(messages.getMessage("insufficientMoney").replace("{price}", BalanceFormatter.format(priceAll)));
                        return;
                    }
                    final SpawnerItem spawnerItem = new SpawnerItem().parse(entityType);
                    spawnerItem.setAmountSpawners(value);
                    spawnerItem.setAmountItem(1);
                    spawnerItem.giveItem(p);
                    economy.removeMoney(p.getName(), priceAll);
                    p.sendMessage(messages.getMessage("buySpawner").replace("{price}", BalanceFormatter.format(priceAll)).replace("{amount}", BalanceFormatter.format(value)));
                    trCached.remove(obj);
                } catch (Exception ignored) {
                    p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                }
            }
        }
    }
}
