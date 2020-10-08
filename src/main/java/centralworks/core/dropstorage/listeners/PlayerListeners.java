package centralworks.core.dropstorage.listeners;

import centralworks.Main;
import centralworks.core.commons.cache.ICached;
import centralworks.core.commons.cache.LimitCached;
import centralworks.core.commons.models.Impulse;
import centralworks.core.commons.models.Limit;
import centralworks.core.commons.models.enums.ImpulseType;
import centralworks.core.commons.models.enums.LimitType;
import centralworks.core.dropstorage.models.BoosterPlayer;
import centralworks.core.dropstorage.models.DropStorage;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Configuration;
import centralworks.lib.FormatTime;
import centralworks.database.SyncRequests;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.enums.TaskType;
import centralworks.core.spawners.cache.TCached;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    public void onJoin(PlayerJoinEvent event) {
        final SyncRequests<DropStorage, Object> query = new DropStorage(event.getPlayer()).query();
        final DropStorage storage = query.persist();
        storage.fixDrops();
        storage.applyBoostersDefault();
        storage.fixBonus(event.getPlayer());
        query.commit();
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        final SyncRequests<DropStorage, Object> query = new DropStorage(event.getPlayer()).query();
        query.persist();
        query.commit();
    }

    @EventHandler
    public void onTalk(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        final TCached trCached = TCached.get();
        if (trCached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
            e.setCancelled(true);
            final String msg = e.getMessage();
            final Configuration messages = Main.getInstance().getMessages();
            final TCached.TaskObj obj = trCached.get(taskObj -> taskObj.getPlayerName().equalsIgnoreCase(p.getName()));
            if (msg.equalsIgnoreCase("cancelar")) {
                p.sendMessage(messages.getMessage("cancel"));
                trCached.remove(obj);
                return;
            }
            if (obj.getTaskType() == TaskType.ADD_FRIEND) {
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
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        final Player p = event.getPlayer();
        final ItemStack item = event.getItem();
        final Configuration messages = Main.getInstance().getMessages();
        if (item == null) return;

        if (Main.getInstance().limitSystemIsActive()) {
            for (Limit limit : LimitCached.get().getList()) {
                if (!item.isSimilar(limit.getItemStack().getAsItem(s -> s))) continue;
                event.setCancelled(true);
                final LimitType type = limit.getLimitType();
                if (!limit.hasPermission(p)) {
                    p.sendMessage(messages.getMessage("permissionErrorLimit").replace("{type}", type.getName()));
                    return;
                }
                final UserDetails userDetails = new UserDetails(p).query().persist();
                final Double max = Main.getInstance().getDropStorage().getDouble("Limits.max");
                if (type == LimitType.SELL) {
                    if (userDetails.getSellLimit() >= max) {
                        p.sendMessage(messages.getMessage("limitMax").replace("{type}", type.getName()));
                        return;
                    }
                    final Double adder = userDetails.getSellLimit() + limit.getValue() > max ?
                            limit.getValue() - (userDetails.getSellLimit() + limit.getValue() - max)
                            : limit.getValue();

                    userDetails.addSellLimit(adder);
                    final Double v = userDetails.getSellLimit();
                    userDetails.query().commit();
                    if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                    else p.setItemInHand(new ItemStack(Material.AIR));
                    p.sendMessage(messages.getMessage("limitUsed")
                                    .replace("{type}", type.getName())
                                    .replace("{limit-value}", BalanceFormatter.format(adder))
                                    .replace("{player-limit-value}", BalanceFormatter.format(v)));
                    return;
                }
            }
        }

        for (Impulse booster : ICached.get().getList()) {
            if (!item.isSimilar(booster.getAsItem())) continue;
            event.setCancelled(true);
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
                dropStorage.addBooster(new BoosterPlayer(dropStorage, booster.getValue(), booster.getTime()));
                p.sendMessage(messages.getMessage("booster-used").replace("{time}", new FormatTime(TimeUnit.SECONDS.toMillis(booster.getTime())).format()).replace("{multiplier}", booster.getValue().toString()));
            }
            dropStorage.query().commit();
            if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
            else p.setItemInHand(new ItemStack(Material.AIR));
            return;
        }
    }
}
