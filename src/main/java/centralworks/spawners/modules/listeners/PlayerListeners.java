package centralworks.spawners.modules.listeners;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.QueriesSync;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.lib.FormatTime;
import centralworks.spawners.modules.events.BoosterActiveEvent;
import centralworks.spawners.modules.models.Settings;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.addons.*;
import centralworks.spawners.modules.models.dropsstorage.BoosterPlayer;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerImpulse;
import centralworks.spawners.modules.models.spawners.SpawnerItem;
import centralworks.spawners.modules.models.spawners.TaskType;
import centralworks.spawners.modules.models.spawners.cached.TCached;
import de.tr7zw.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final ItemStack item = e.getItem();
        final Block block = e.getClickedBlock();
        final Settings se = Settings.get();
        final Configuration messages = Main.getMessages();
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
            for (Limit limit : LimitCached.get().getList()) {
                if (!item.isSimilar(limit.getItemStack().getAsItem(s -> s))) continue;
                e.setCancelled(true);
                final LimitType type = limit.getLimitType();
                if (!limit.hasPermission(p)) {
                    p.sendMessage(messages.getMessage("permissionErrorLimit").replace("{type}", type.getName()));
                    return;
                }
                final UserDetails userDetails = new UserDetails(p).query().persist();
                final Double max = Main.getLimits().getDouble("Limits.max");
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
                } else if (type == LimitType.SELL) {
                    if (userDetails.getSellLimit() >= max) {
                        p.sendMessage(messages.getMessage("limitMax").replace("{type}", type.getName()));
                        return;
                    }
                    if (userDetails.getSellLimit() + limit.getValue() > max)
                        adder = limit.getValue() - (userDetails.getSellLimit() + limit.getValue() - max);
                    else adder = limit.getValue();
                    userDetails.addSellLimit(adder);
                    v = userDetails.getSellLimit();
                } else return;
                userDetails.query().commit();
                if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                else p.setItemInHand(new ItemStack(Material.AIR));
                p.sendMessage(messages.getMessage("limitUsed").replace("{type}", type.getName()).replace("{limit-value}", FormatBalance.format(adder)).replace("{player-limit-value}", FormatBalance.format(v)));
                return;
            }

            for (Impulse booster : ICached.get().getList()) {
                if (!item.isSimilar(booster.getAsItem())) continue;
                e.setCancelled(true);
                if (booster.getType() != ImpulseType.DROPS) return;
                if (!booster.hasPermission(p)) {
                    p.sendMessage(messages.getMessage("permission-error-booster"));
                    return;
                }
                final DropStorage dropStorage = new DropStorage(p).query().persist();
                if (booster.getTime() == 0) {
                    dropStorage.addMultiplier(booster.getValue());
                    p.sendMessage(messages.getMessage("booster-used").replace("{time}", "infinito").replace("{multiplier}", booster.getValue().toString()));
                } else {
                    dropStorage.addBooster(new BoosterPlayer(booster.getValue(), booster.getTime()));
                    p.sendMessage(messages.getMessage("booster-used").replace("{time}", new FormatTime(TimeUnit.SECONDS.toMillis(booster.getTime())).format()).replace("{multiplier}", booster.getValue().toString()));
                }
                dropStorage.query().commit();
                if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                else p.setItemInHand(new ItemStack(Material.AIR));
                return;
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
            final Configuration messages = Main.getMessages();
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
                        p.sendMessage(messages.getMessage("spawnerRemoved2").replace("{amount}", FormatBalance.format(v)));
                        spawner.query().commit();
                    } catch (Exception ex) {
                        p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                    }
                }, exception -> trCached.remove(obj));
            } else if (obj.getTaskType() == TaskType.ADD_FRIEND) {
                new Spawner(obj.getValue()).query().ifExists(spawner -> {
                    if (!spawner.isOwner(p.getName())) {
                        trCached.remove(obj);
                        return;
                    }
                    if (Bukkit.getPlayer(msg) == null) {
                        p.sendMessage(messages.getMessage("offlinePlayer").replace("{player}", msg));
                        return;
                    }
                    if (spawner.existsFriend(msg)) {
                        p.sendMessage(messages.getMessage("alreadyHaveFriend").replace("{player}", msg));
                        return;
                    }
                    spawner.addFriend(msg);
                    spawner.query().commit();
                    p.sendMessage(messages.getMessage("friendAdded").replace("{player}", msg));
                    trCached.remove(obj);
                }, exception -> trCached.remove(obj));
            } else if (obj.getTaskType() == TaskType.BUY_SPAWNERS) {
                final EntityType entityType = EntityType.valueOf(obj.getValue());
                final Double price = Main.getSpawners().getDouble("List." + entityType.name() + ".price");
                try {
                    final Double value = Math.ceil(Double.parseDouble(msg));
                    final UserDetails user = new UserDetails(p).query().persist();
                    if (value < 0 || value.isNaN()) {
                        p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                        return;
                    }
                    if (user.getBuyLimit() < value) {
                        p.sendMessage(messages.getMessage("insufficientLimit").replace("{amount}", FormatBalance.format(user.getBuyLimit())));
                        return;
                    }
                    final double priceAll = value * price;
                    final Economy eco = Main.getEconomy();
                    if (eco.getBalance(p) < priceAll) {
                        p.sendMessage(messages.getMessage("insufficientMoney").replace("{price}", FormatBalance.format(priceAll)));
                        return;
                    }
                    final SpawnerItem spawnerItem = new SpawnerItem().parse(entityType);
                    spawnerItem.setAmountSpawners(value);
                    spawnerItem.setAmountItem(1);
                    spawnerItem.giveItem(p);
                    eco.withdrawPlayer(p, priceAll);
                    p.sendMessage(messages.getMessage("buySpawner").replace("{price}", FormatBalance.format(priceAll)).replace("{amount}", FormatBalance.format(value)));
                    trCached.remove(obj);
                } catch (Exception ignored) {
                    p.sendMessage(messages.getMessage("invalidNumber").replace("{number}", msg));
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final String name = e.getPlayer().getName();
        final QueriesSync<UserDetails> query = new UserDetails(name).query();
        query.persist().fixLimits();
        query.commit(true);
        final QueriesSync<PlayerQuests> query2 = new PlayerQuests(name).query();
        query2.persist().addQuestsDefaults();
        query2.commit(true);
        final QueriesSync<DropStorage> query3 = new DropStorage(name).query();
        query3.persist().fixDrops();
        query3.persist().applyBoostersDefault();
        query3.persist().fixBonus(e.getPlayer());
        query3.commit(true);
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        new UserDetails(e.getPlayer()).query().commit(true);
        new PlayerQuests(e.getPlayer()).query().commit(true);
        new DropStorage(e.getPlayer()).query().commit(true);
    }
}
