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
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
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

public class SpawnerListeners {

    private static final Main plugin;
    private static final boolean breakWithSilkTouch;
    @Getter
    private static final Listener listener;

    static {
        plugin = Main.getInstance();
        val factionMode = plugin.getSpawners().navigate().getBoolean("FactionMode");
        breakWithSilkTouch = plugin.getSpawners().navigate().getBoolean("BreakWithSilkTouch");
        listener = factionMode ? new WithoutSimpleMode() : new WithSimpleMode();
    }

    public static class WithoutSimpleMode extends Commons {

        @EventHandler
        public void onInteract(PlayerInteractEvent e) {
            val block = e.getClickedBlock();
            if (block != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (block.getType() != Material.getMaterial("MOB_SPAWNER")) return;
                val location = block.getLocation();
                val p = e.getPlayer();
                val user = Caches.getCache(User.class).getIfPresent(p.getName());
                if (user.exists(location)) {
                    e.setCancelled(true);
                    val spawner = user.getSpawner(location);
                    new InfoSpawnerMenu(spawner, p).load();
                } else {
                    val cache = Caches.getCache(Spawner.class);
                    Optional.ofNullable(cache.getIfPresent(LocationUtils.locToString(location))).ifPresent(spawner -> {
                        if (spawner.hasPermission(p.getName())) new InfoSpawnerMenu(spawner, p).load();
                        else p.sendMessage(plugin.getMessages().navigate().getMessage("isNotOwner"));
                    });
                }
            }
        }

        @EventHandler
        public void onPlace(BlockPlaceEvent e) {
            val item = e.getItemInHand();
            if (!SpawnerItem.isSpawnerItem(item)) return;
            e.setCancelled(true);
            val location = e.getBlock().getLocation();
            val p = e.getPlayer();
            val spawnerItem = new SpawnerItem(item);
            val cache = Caches.getCache(Spawner.class);
            final Predicate<ItemStack> prd = itemStack -> SpawnerItem.isSpawnerItem(itemStack) && spawnerItem.isSimilar(new SpawnerItem(itemStack));
            val spawnerItem1 = Arrays.stream(p.getInventory().getContents())
                    .filter(prd)
                    .map(SpawnerItem::new)
                    .reduce(SpawnerItem::concat).get();
            val spawner = new SpawnerBuilder(location, p.getName()).build(spawnerItem1);
            val user = Caches.getCache(User.class).getIfPresent(p.getName());
            user.getSpawners(spawners -> {
                val functions = new FilteringFunctions(spawners);
                if (functions.exists(spawner.getEntityType())) {
                    val spawner1 = functions.get(spawner.getEntityType());
                    val event = new SpawnerStackEvent(p, spawner1, spawner);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) return;
                    spawner1.concat(spawner);
                } else {
                    val event = new SpawnerPlaceEvent(p, spawner);
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
            val block = e.getBlock();
            if (block.getType() != Material.getMaterial("MOB_SPAWNER")) return;
            val location = block.getLocation();
            val p = e.getPlayer();
            val item = p.getItemInHand();
            val user = Caches.getCache(User.class).getIfPresent(p.getName());
            if (user.exists(location)) {
                e.setCancelled(true);
                if (Delay.inDelay(p.getName())) {
                    p.sendMessage("§cAguarde para poder retirar o gerador novamente.");
                    return;
                }
                val spawner = user.getSpawner(location);
                final SpawnerItem spawnerItem = new SpawnerItem(spawner);
                if (breakWithSilkTouch) {
                    if (item.containsEnchantment(Enchantment.SILK_TOUCH)) boom(user, spawner, spawnerItem);
                    else spawner.destroy(user);
                } else boom(user, spawner, spawnerItem);
            } else {
                Optional.ofNullable(Caches.getCache(Spawner.class).getIfPresent(LocationUtils.locToString(location))).ifPresent(spawner -> {
                    e.setCancelled(true);
                    p.sendMessage(plugin.getMessages().navigate().getMessage("isNotOwner"));
                });
            }
        }

    }

    public static class WithSimpleMode extends Commons {

        @EventHandler
        public void onPlace(BlockPlaceEvent e) {
            val item = e.getItemInHand();
            if (!SpawnerItem.isSpawnerItem(item)) return;
            val l = e.getBlock().getLocation();
            val spawnerItem = new SpawnerItem(item);
            l.getBlock().setType(Material.getMaterial("MOB_SPAWNER"));
            val spawnerBlock = ((CreatureSpawner) l.getBlock().getState());
            spawnerBlock.setCreatureTypeByName(spawnerItem.getEntityType().name());
            spawnerBlock.setSpawnedType(spawnerItem.getEntityType());
            spawnerBlock.setDelay(20);
            spawnerBlock.update();
        }

        @EventHandler
        public void onBreak(BlockBreakEvent e) {
            val block = e.getBlock();
            if (block.getType() == Material.getMaterial("MOB_SPAWNER")) return;
            val p = e.getPlayer();
            val item = p.getItemInHand();
            val entityType = ((CreatureSpawner) block.getState()).getSpawnedType();
            e.setCancelled(true);
            if (Delay.inDelay(p.getName())) {
                p.sendMessage("§cAguarde para poder retirar o gerador novamente.");
                return;
            }
            block.setType(Material.AIR);
            val spawnerItem = new SpawnerItem(entityType);
            if (breakWithSilkTouch) {
                if (item.containsEnchantment(Enchantment.SILK_TOUCH)) spawnerItem.giveItem(p);
            } else spawnerItem.giveItem(p);
        }

    }

    public static class Commons implements Listener {

        @EventHandler
        public void onInteractWithEgg(final PlayerInteractEvent e) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.getMaterial("MOB_SPAWNER") && e.getItem() != null && e.getItem().getType() == Material.getMaterial("MONSTER_EGG")) {
                e.setCancelled(true);
            }
        }

    }

    private static void boom(User user, Spawner spawner, SpawnerItem spawnerItem) {
        val p = user.getPlayer();
        val nav = plugin.getMessages().navigate();
        val event = new SpawnerBreakEvent(p, spawner, spawnerItem);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        spawnerItem.giveItem(p);
        spawner.destroy(user);
        p.sendMessage(nav.getMessage("spawnerRemoved"));
        Delay.put(p.getName());
    }

}
