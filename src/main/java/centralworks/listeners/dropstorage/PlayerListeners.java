package centralworks.listeners.dropstorage;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.cache.simple.ICached;
import centralworks.cache.simple.LimitCached;
import centralworks.models.Impulse;
import centralworks.models.Limit;
import centralworks.models.User;
import centralworks.models.enums.ImpulseType;
import centralworks.models.enums.LimitType;
import centralworks.models.BoosterPlayer;
import centralworks.models.ProductStorage;
import centralworks.cache.simple.TCached;
import centralworks.spawners.TaskType;
import centralworks.spawners.models.Spawner;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.FormatTime;
import centralworks.lib.Settings;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final LoadingCache<String, ProductStorage> cache = Caches.getCache(ProductStorage.class);
        final String name = event.getPlayer().getName();
        final ProductStorage storage = Optional.ofNullable(cache.getIfPresent(name)).orElse(new ProductStorage(name));
        storage.fixDrops();
        storage.applyBoostersDefault();
        storage.fixBonus(event.getPlayer());
        cache.put(name, storage);
    }

    @EventHandler
    public void onTalk(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        final TCached trCached = TCached.get();
        if (trCached.exists(s -> s.getPlayerName().equalsIgnoreCase(p.getName()))) {
            e.setCancelled(true);
            final String msg = e.getMessage();
            final Settings.Navigate nav = Main.getInstance().getMessages().navigate();
            final TCached.TaskObj obj = trCached.get(taskObj -> taskObj.getPlayerName().equalsIgnoreCase(p.getName()));
            if (msg.equalsIgnoreCase("cancelar")) {
                p.sendMessage(nav.getMessage("cancel"));
                trCached.remove(obj);
                return;
            }
            if (obj.getTaskType() == TaskType.ADD_FRIEND) {
                trCached.remove(obj);

                Optional.ofNullable(cache.getIfPresent(obj.getValue())).ifPresent(spawner -> {
                    if (!spawner.isOwner(p.getName())) return;
                    if (Bukkit.getPlayer(msg) == null) {
                        p.sendMessage(nav.getMessage("offlinePlayer").replace("{player}", msg));
                        return;
                    }
                    if (spawner.existsFriend(msg)) {
                        p.sendMessage(nav.getMessage("alreadyHaveFriend").replace("{player}", msg));
                        return;
                    }
                    spawner.addFriend(msg);
                    p.sendMessage(nav.getMessage("friendAdded").replace("{player}", msg));
                });
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        final ItemStack item = event.getItem();
        final Settings.Navigate nav = Main.getInstance().getMessages().navigate();
        if (item == null) return;

        if (Main.getInstance().limitSystemIsActive()) {
            final LoadingCache<String, User> cache = Caches.getCache(User.class);
            final User user = cache.getIfPresent(p.getName());
            for (Limit limit : LimitCached.get().getList()) {
                if (!item.isSimilar(limit.getItemStack().getAsItem(s -> s))) continue;
                event.setCancelled(true);
                final LimitType type = limit.getLimitType();
                if (!limit.hasPermission(p)) {
                    p.sendMessage(nav.getMessage("permissionErrorLimit").replace("{type}", type.getName()));
                    return;
                }
                final Double max = Main.getInstance().getDropStorage().navigate().getDouble("Limits.max");
                if (type == LimitType.SELL) {
                    if (user.getSellLimit() >= max) {
                        p.sendMessage(nav.getMessage("limitMax").replace("{type}", type.getName()));
                        return;
                    }
                    final Double adder = user.getSellLimit() + limit.getValue() > max ?
                            limit.getValue() - (user.getSellLimit() + limit.getValue() - max)
                            : limit.getValue();

                    user.addSellLimit(adder);
                    final Double v = user.getSellLimit();
                    if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                    else p.setItemInHand(new ItemStack(Material.AIR));
                    p.sendMessage(nav.getMessage("limitUsed")
                            .replace("{type}", type.getName())
                            .replace("{limit-value}", BalanceFormatter.format(adder))
                            .replace("{player-limit-value}", BalanceFormatter.format(v)));
                    return;
                }
            }
        }

        final LoadingCache<String, ProductStorage> cache = Caches.getCache(ProductStorage.class);
        final ProductStorage productStorage = cache.getIfPresent(p.getName());

        for (Impulse booster : ICached.get().getList()) {
            if (!item.isSimilar(booster.getAsItem())) continue;
            event.setCancelled(true);
            if (booster.getType() != ImpulseType.DROPS) return;
            if (!booster.hasPermission(p)) {
                p.sendMessage(nav.getMessage("permission-error-booster"));
                return;
            }
            if (booster.getTime() == 0) {
                productStorage.addMultiplier(booster.getValue());
                p.sendMessage(nav.getMessage("booster-used").replace("{time}", "infinito").replace("{multiplier}", booster.getValue().toString()));
            } else {
                productStorage.addBooster(new BoosterPlayer(productStorage, booster.getValue(), booster.getTime()));
                p.sendMessage(nav.getMessage("booster-used").replace("{time}", new FormatTime(TimeUnit.SECONDS.toMillis(booster.getTime())).format()).replace("{multiplier}", booster.getValue().toString()));
            }
            if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
            else p.setItemInHand(new ItemStack(Material.AIR));
            return;
        }
    }
}
