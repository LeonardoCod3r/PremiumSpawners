package centralworks.core.spawners.listeners;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.spawners.cache.DCached;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerBuilder;
import centralworks.core.spawners.models.SpawnerItem;
import centralworks.core.spawners.utils.FilteringFunctions;
import centralworks.layouts.InfoSpawnerMenu;
import centralworks.lib.Utils;
import lombok.var;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class SpawnerListeners implements Listener {

    private final Main plugin;

    public SpawnerListeners() {
        this.plugin = Main.getInstance();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        var block = e.getClickedBlock();
        if (block != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block.getType() != Material.MOB_SPAWNER) return;
            var location = block.getLocation();
            var p = e.getPlayer();
            var user = Caches.getCache(UserDetails.class).getUnchecked(p.getName());
            if (user.exists(location)) {
                e.setCancelled(true);
                var spawner = user.getSpawner(location);
                new InfoSpawnerMenu(spawner, p).load();
            } else {
                var cache = Caches.getCache(Spawner.class);
                Optional.ofNullable(cache.getIfPresent(Utils.locToString(location))).ifPresent(spawner -> {
                    if (spawner.hasPermission(p.getName())) new InfoSpawnerMenu(spawner, p).load();
                    else p.sendMessage(plugin.getMessages().getMessage("isNotOwner"));
                });
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        var item = e.getItemInHand();
        if (!new SpawnerItem().isSpawnerItem(item)) return;
        e.setCancelled(true);
        var p = e.getPlayer();
        var location = e.getBlock().getLocation();
        var spawnerItem = new SpawnerItem().parse(item);
        var cache = Caches.getCache(Spawner.class);
        final Predicate<ItemStack> prd = itemStack -> new SpawnerItem().isSpawnerItem(itemStack) && spawnerItem.isSimilar(itemStack);
        var cached = DCached.get();
        if (cached.exists(s -> s.equalsIgnoreCase(p.getName()))) {
            p.sendMessage("§cAguarde para poder colocar o gerador novamente.");
            return;
        }
        var spawnerItem1 = Arrays.stream(p.getInventory().getContents())
                .filter(prd)
                .map(itemStack -> new SpawnerItem().parse(itemStack))
                .reduce(SpawnerItem::concat).get();
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (prd.test(p.getInventory().getItem(i))) {
                p.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        }
        var spawner = new SpawnerBuilder(location, p.getName()).build(spawnerItem1);
        var user = Caches.getCache(UserDetails.class).getUnchecked(p.getName());
        user.getSpawners(spawners -> {
            var functions = new FilteringFunctions(spawners);
            if (functions.exists(spawner.getEntityType())) {
                var spawner1 = functions.get(spawner.getEntityType());
                spawner1.concat(spawner);
            } else {
                user.addSpawnerLocation(location);
                spawner.appear(spawner1 -> cache.put(spawner1.getLocSerialized(), spawner1));
            }
        });
        cached.add(p.getName());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        var block = e.getBlock();
        if (block.getType() != Material.MOB_SPAWNER) return;
        var location = block.getLocation();
        var p = e.getPlayer();
        var messages = plugin.getMessages();
        var user = Caches.getCache(UserDetails.class).getUnchecked(p.getName());
        var cached = DCached.get();

        if (user.exists(location)) {
            e.setCancelled(true);
            if (cached.exists(s -> s.equalsIgnoreCase(p.getName()))) {
                p.sendMessage("§cAguarde para poder retirar o gerador novamente.");
                return;
            }
            var spawner = user.getSpawner(location);
            new SpawnerItem().parse(spawner).giveItem(p);
            spawner.destroy(user);
            p.sendMessage(messages.getMessage("spawnerRemoved"));
            cached.add(p.getName());
        } else {
            Optional.ofNullable(Caches.getCache(Spawner.class).getUnchecked(Utils.locToString(location))).ifPresent(spawner -> {
                e.setCancelled(true);
                p.sendMessage(messages.getMessage("isNotOwner"));
            });
        }
    }

}
