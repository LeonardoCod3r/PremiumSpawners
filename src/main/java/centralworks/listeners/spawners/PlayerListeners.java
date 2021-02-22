package centralworks.listeners.spawners;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.cache.simple.LimitCached;
import centralworks.cache.simple.TCached;
import centralworks.events.BoosterActiveEvent;
import centralworks.events.SpawnerRemoveEvent;
import centralworks.hooks.EconomyContext;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Settings;
import centralworks.lib.enums.PluginSystemType;
import centralworks.models.Limit;
import centralworks.models.User;
import centralworks.models.enums.ImpulseType;
import centralworks.models.enums.LimitType;
import centralworks.spawners.Identifiers;
import centralworks.spawners.TaskType;
import centralworks.spawners.models.Spawner;
import centralworks.spawners.models.SpawnerImpulse;
import centralworks.spawners.models.SpawnerItem;
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
        final Settings.Navigate nav = plugin.getMessages().navigate();
        if (item == null) return;
        try {
            final NBTItem nbt = new NBTItem(item);
            final Double multiplier = nbt.getDouble(Identifiers.NBT_TAG_BOOSTER_VALUE);
            final Integer delay = nbt.getInteger(Identifiers.NBT_TAG_BOOSTER_DELAY);
            final ImpulseType type = ImpulseType.valueOf(nbt.getString(Identifiers.NBT_TAG_BOOSTER_TYPE));
            e.setCancelled(true);
            if (type == ImpulseType.DROPS) return;
            final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
            Optional.ofNullable(cache.getIfPresent(Main.getGson().toJson(block.getLocation()))).ifPresent(spawner -> {
                if (!spawner.isOwner(p.getName())) {
                    p.sendMessage(nav.getMessage("onlyActiveBooster"));
                    return;
                }
                if (spawner.hasBoosterActive(type)) {
                    p.sendMessage(nav.getMessage("onlyBooster").replace("{type}", type.name()));
                    return;
                }
                final SpawnerImpulse spawnerImpulse = new SpawnerImpulse(type, delay, multiplier);
                final BoosterActiveEvent event = new BoosterActiveEvent(p, spawnerImpulse, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                final String name = p.getName();
                if (item.getAmount() == 1) p.setItemInHand(new ItemStack(Material.AIR));
                else item.setAmount(item.getAmount() - 1);
                p.sendMessage(nav.getMessage("boosterActivated").replace("{type}", type.name()));
                spawnerImpulse.in(spawner, () -> {
                    if (Bukkit.getPlayer(name) != null)
                        Bukkit.getPlayer(name).sendMessage(nav.getMessage("boosterEnd").replace("{type}", type.name()));
                });
            });

        } catch (Exception ignored) {
            if (plugin.limitSystemIsActive()) {
                final LoadingCache<String, User> cache = Caches.getCache(User.class);
                final User user = cache.getIfPresent(p.getName());

                for (Limit limit : LimitCached.get().getList()) {
                    if (!item.isSimilar(limit.getItemStack().getAsItem(s -> s))) continue;
                    e.setCancelled(true);
                    final LimitType type = limit.getLimitType();
                    if (type != LimitType.BUY) continue;
                    if (!limit.hasPermission(p)) {
                        p.sendMessage(nav.getMessage("permissionErrorLimit").replace("{type}", type.getName()));
                        return;
                    }
                    final Double max = plugin.getDropStorage().navigate().getDouble("Limits.max");
                    final Double adder = user.getBuyLimit() + limit.getValue() > max ?
                            limit.getValue() - (user.getBuyLimit() + limit.getValue() - max) : limit.getValue();
                    if (user.getBuyLimit() >= max) {
                        p.sendMessage(nav.getMessage("limitMax").replace("{type}", type.getName()));
                        return;
                    }
                    user.addBuyLimit(adder);
                    final Double v = user.getBuyLimit();
                    if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                    else p.setItemInHand(new ItemStack(Material.AIR));
                    p.sendMessage(nav.getMessage("limitUsed").replace("{type}", type.getName()).replace("{limit-value}", BalanceFormatter.format(adder)).replace("{player-limit-value}", BalanceFormatter.format(v)));
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
            final Settings.Navigate nav = plugin.getMessages().navigate();
            final TCached.TaskObj obj = trCached.get(taskObj -> taskObj.getPlayerName().equalsIgnoreCase(p.getName()));
            if (msg.equalsIgnoreCase("cancelar")) {
                p.sendMessage(nav.getMessage("cancel"));
                trCached.remove(obj);
                return;
            }
            final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
            final User user = Caches.getCache(User.class).getIfPresent(p.getName());
            if (obj.getTaskType() == TaskType.REMOVE_SPAWNERS) {
                trCached.remove(obj);
                Optional.ofNullable(cache.getIfPresent(obj.getValue())).ifPresent(spawner -> {
                    if (!spawner.isOwner(p.getName())) return;
                    try {
                        final double v = Math.ceil(Double.parseDouble(msg));
                        if (v < 1.0) {
                            p.sendMessage(nav.getMessage("invalidNumber").replace("{number}", msg));
                            return;
                        }
                        if (v > spawner.getAmount()) {
                            p.sendMessage(nav.getMessage("insufficientSpawners"));
                            return;
                        }
                        if (v == spawner.getAmount()) {
                            final SpawnerItem spawnerItem = new SpawnerItem(spawner);
                            final SpawnerRemoveEvent event = new SpawnerRemoveEvent(p, spawner, spawnerItem, true);
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) return;
                            spawnerItem.giveItem(p);
                            spawner.destroy(user);
                            p.sendMessage(nav.getMessage("spawnerRemoved"));
                            return;
                        }
                        final SpawnerItem spawnerItem = new SpawnerItem(spawner);
                        spawnerItem.setAmountSpawners(v);
                        final SpawnerRemoveEvent event = new SpawnerRemoveEvent(p, spawner, spawnerItem, true);
                        Bukkit.getPluginManager().callEvent(event);
                        if (event.isCancelled()) return;
                        final Settings.Navigate navSpawner = Main.getInstance().getSpawners().navigate();
                        final boolean hologram = navSpawner.getBoolean("HologramToggle");
                        spawnerItem.giveItem(p);
                        spawner.removeStack(v);
                        spawner.updateHologram(hologram);
                        p.sendMessage(nav.getMessage("spawnerRemoved2").replace("{amount}", BalanceFormatter.format(v)));
                    } catch (Exception ex) {
                        p.sendMessage(nav.getMessage("invalidNumber").replace("{number}", msg));
                    }
                });
            } else if (obj.getTaskType() == TaskType.BUY_SPAWNERS) {
                final EntityType entityType = EntityType.valueOf(obj.getValue());
                final Double price = plugin.getSpawners().navigate().getDouble("List." + entityType.name() + ".price");
                try {
                    final Double value = Math.ceil(Double.parseDouble(msg));
                    if (value < 1 || value.isNaN()) {
                        p.sendMessage(nav.getMessage("invalidNumber").replace("{number}", msg));
                        return;
                    }
                    if (plugin.limitSystemIsActive() && user.getBuyLimit() < value) {
                        p.sendMessage(nav.getMessage("insufficientLimit").replace("{amount}", BalanceFormatter.format(user.getBuyLimit())));
                        return;
                    }
                    final double priceAll = value * price;
                    final EconomyContext.Economy economy = EconomyContext.getContext(PluginSystemType.SPAWNERS_BUY).getEconomy();
                    if (economy.getBalance(p.getName()) < priceAll) {
                        p.sendMessage(nav.getMessage("insufficientMoney").replace("{price}", BalanceFormatter.format(priceAll)));
                        return;
                    }
                    final SpawnerItem spawnerItem = new SpawnerItem(entityType);
                    spawnerItem.setAmountSpawners(value);
                    spawnerItem.setAmountItem(1);
                    spawnerItem.giveItem(p);
                    economy.removeMoney(p.getName(), priceAll);
                    p.sendMessage(nav.getMessage("buySpawner").replace("{price}", BalanceFormatter.format(priceAll)).replace("{amount}", BalanceFormatter.format(value)));
                    trCached.remove(obj);
                } catch (Exception ignored) {
                    p.sendMessage(nav.getMessage("invalidNumber").replace("{number}", msg));
                }
            }
        }
    }
}
