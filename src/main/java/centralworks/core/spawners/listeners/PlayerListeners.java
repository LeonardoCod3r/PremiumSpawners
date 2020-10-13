package centralworks.core.spawners.listeners;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.commons.cache.LimitCached;
import centralworks.core.commons.models.Limit;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.commons.models.enums.ImpulseType;
import centralworks.core.commons.models.enums.LimitType;
import centralworks.core.spawners.Settings;
import centralworks.core.spawners.cache.TCached;
import centralworks.core.spawners.events.BoosterActiveEvent;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
import centralworks.core.spawners.models.SpawnerItem;
import centralworks.core.spawners.models.enums.TaskType;
import centralworks.hooks.EconomyContext;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Configuration;
import centralworks.lib.Utils;
import centralworks.lib.enums.PluginSystemType;
import com.google.common.cache.LoadingCache;
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

import java.util.Optional;

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
            final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
            Optional.ofNullable(cache.getIfPresent(Utils.locToString(block.getLocation()))).ifPresent(spawner -> {
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
                spawnerImpulse.in(spawner, () -> {
                    if (Bukkit.getPlayer(name) != null)
                        Bukkit.getPlayer(name).sendMessage(messages.getMessage("boosterEnd").replace("{type}", type.name()));
                });
            });

        } catch (Exception ignored) {
            if (plugin.limitSystemIsActive()) {
                final LoadingCache<String, UserDetails> cache = Caches.getCache(UserDetails.class);
                final UserDetails userDetails = cache.getUnchecked(p.getName());

                for (Limit limit : LimitCached.get().getList()) {
                    if (!item.isSimilar(limit.getItemStack().getAsItem(s -> s))) continue;
                    e.setCancelled(true);
                    final LimitType type = limit.getLimitType();
                    if (type != LimitType.BUY) continue;
                    if (!limit.hasPermission(p)) {
                        p.sendMessage(messages.getMessage("permissionErrorLimit").replace("{type}", type.getName()));
                        return;
                    }
                    final Double max = plugin.getDropStorage().getDouble("Limits.max");
                    final Double adder = userDetails.getBuyLimit() + limit.getValue() > max ?
                            limit.getValue() - (userDetails.getBuyLimit() + limit.getValue() - max) : limit.getValue();
                    if (userDetails.getBuyLimit() >= max) {
                        p.sendMessage(messages.getMessage("limitMax").replace("{type}", type.getName()));
                        return;
                    }
                    userDetails.addBuyLimit(adder);
                    final Double v = userDetails.getBuyLimit();
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
            final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
            final UserDetails user = Caches.getCache(UserDetails.class).getUnchecked(p.getName());
            if (obj.getTaskType() == TaskType.REMOVE_SPAWNERS) {
                trCached.remove(obj);
                Optional.ofNullable(cache.getIfPresent(obj.getValue())).ifPresent(spawner -> {
                    if (!spawner.isOwner(p.getName())) return;
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
                        if (v == spawner.getAmount()) {
                            new SpawnerItem().parse(spawner).giveItem(p);
                            spawner.destroy(user);
                            p.sendMessage(messages.getMessage("spawnerRemoved"));
                            return;
                        }
                        final SpawnerItem spawnerItem = new SpawnerItem().parse(spawner);
                        spawnerItem.setAmountSpawners(v);
                        spawnerItem.giveItem(p);
                        spawner.removeStack(v);
                        spawner.updateHologram();
                        p.sendMessage(messages.getMessage("spawnerRemoved2").replace("{amount}", BalanceFormatter.format(v)));
                    } catch (Exception ex) {
                        p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                    }
                });
            } else if (obj.getTaskType() == TaskType.BUY_SPAWNERS) {
                final EntityType entityType = EntityType.valueOf(obj.getValue());
                final Double price = plugin.getSpawners().getDouble("List." + entityType.name() + ".price");
                try {
                    final Double value = Math.ceil(Double.parseDouble(msg));
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
