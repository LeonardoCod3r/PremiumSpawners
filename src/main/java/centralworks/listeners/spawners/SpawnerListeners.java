package centralworks.listeners.spawners;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.User;
import centralworks.cache.simple.Delay;
import centralworks.events.SpawnerBreakEvent;
import centralworks.events.SpawnerPlaceEvent;
import centralworks.events.SpawnerStackEvent;
import centralworks.spawners.models.Spawner;
import centralworks.spawners.models.SpawnerBuilder;
import centralworks.spawners.models.SpawnerItem;
import centralworks.spawners.utils.FilteringFunctions;
import centralworks.layouts.menus.spawner.InfoSpawnerMenu;
import centralworks.lib.LocationUtils;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
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
    private final boolean factionMode;

    public SpawnerListeners() {
        this.plugin = Main.getInstance();
        this.factionMode = plugin.getSpawners().navigate().getBoolean("FactionMode");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (factionMode) return;
        var block = e.getClickedBlock();
        if (block != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block.getType() != Material.MOB_SPAWNER) return;
            var location = block.getLocation();
            var p = e.getPlayer();
            var user = Caches.getCache(User.class).getIfPresent(p.getName());
            if (user.exists(location)) {
                e.setCancelled(true);
                var spawner = user.getSpawner(location);
                new InfoSpawnerMenu(spawner, p).load();
            } else {
                var cache = Caches.getCache(Spawner.class);
                Optional.ofNullable(cache.getIfPresent(LocationUtils.locToString(location))).ifPresent(spawner -> {
                    if (spawner.hasPermission(p.getName())) new InfoSpawnerMenu(spawner, p).load();
                    else p.sendMessage(plugin.getMessages().navigate().getMessage("isNotOwner"));
                });
            }
        }
    }

    @EventHandler
    public void onPlaceFM(BlockPlaceEvent e) {
        var item = e.getItemInHand();
        if (!SpawnerItem.isSpawnerItem(item) || !factionMode) return;
        val l = e.getBlock().getLocation();
        var spawnerItem = new SpawnerItem(item);
        l.getBlock().setType(Material.MOB_SPAWNER);
        final CreatureSpawner spawnerBlock = ((CreatureSpawner) l.getBlock().getState());
        spawnerBlock.setCreatureTypeByName(spawnerItem.getEntityType().name());
        spawnerBlock.setSpawnedType(spawnerItem.getEntityType());
        spawnerBlock.setDelay(20);
        spawnerBlock.update();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        var item = e.getItemInHand();
        if (!SpawnerItem.isSpawnerItem(item) || factionMode) return;
        e.setCancelled(true);
        var location = e.getBlock().getLocation();
        var p = e.getPlayer();
        var spawnerItem = new SpawnerItem(item);
        var cache = Caches.getCache(Spawner.class);
        final Predicate<ItemStack> prd = itemStack -> SpawnerItem.isSpawnerItem(itemStack) && spawnerItem.isSimilar(new SpawnerItem(itemStack));
        var spawnerItem1 = Arrays.stream(p.getInventory().getContents())
                .filter(prd)
                .map(SpawnerItem::new)
                .reduce(SpawnerItem::concat).get();
        var spawner = new SpawnerBuilder(location, p.getName()).build(spawnerItem1);
        var user = Caches.getCache(User.class).getIfPresent(p.getName());
        user.getSpawners(spawners -> {
            var functions = new FilteringFunctions(spawners);
            if (functions.exists(spawner.getEntityType())) {
                var spawner1 = functions.get(spawner.getEntityType());
                final SpawnerStackEvent event = new SpawnerStackEvent(p, spawner1, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                spawner1.concat(spawner);
            } else {
                final SpawnerPlaceEvent event = new SpawnerPlaceEvent(p, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;
                user.addSpawnerLocation(location);
                spawner.appear(spawner1 -> cache.put(spawner1.getLocSerialized(), spawner1));
            }
            for (int i = 0; i < p.getInventory().getSize(); i++) {
                if (prd.test(p.getInventory().getItem(i))) {
                    p.getInventory().setItem(i, new ItemStack(Material.AIR));
                }
            }
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (factionMode) return;
        var block = e.getBlock();
        if (block.getType() != Material.MOB_SPAWNER) return;
        var location = block.getLocation();
        var p = e.getPlayer();
        var nav = plugin.getMessages().navigate();
        var user = Caches.getCache(User.class).getIfPresent(p.getName());
        if (user.exists(location)) {
            e.setCancelled(true);
            if (Delay.inDelay(p.getName())) {
                p.sendMessage("§cAguarde para poder retirar o gerador novamente.");
                return;
            }
            var spawner = user.getSpawner(location);
            final SpawnerItem spawnerItem = new SpawnerItem().parse(spawner);
            final SpawnerBreakEvent event = new SpawnerBreakEvent(p, spawner, spawnerItem);
            if (event.isCancelled()) return;
            spawnerItem.giveItem(p);
            spawner.destroy(user);
            p.sendMessage(nav.getMessage("spawnerRemoved"));
            Delay.put(p.getName());
        } else {
            Optional.ofNullable(Caches.getCache(Spawner.class).getIfPresent(LocationUtils.locToString(location))).ifPresent(spawner -> {
                e.setCancelled(true);
                p.sendMessage(nav.getMessage("isNotOwner"));
            });
        }
    }



    @EventHandler
    public void onInteractWithEgg(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.MOB_SPAWNER && e.getItem() != null && e.getItem().getType() == Material.MONSTER_EGG) {
            e.setCancelled(true);
        }
    }

}
