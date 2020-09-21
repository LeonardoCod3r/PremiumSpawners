package centralworks.spawners.modules.listeners;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.modules.animations.AnimationBreak;
import centralworks.spawners.modules.animations.AnimationPlace;
import centralworks.spawners.modules.menu.InfoSpawnerMenu;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerItem;
import centralworks.spawners.modules.models.spawners.cached.DCached;
import centralworks.spawners.modules.models.spawners.utils.FilteringFunctions;
import centralworks.spawners.modules.models.spawners.utils.SpawnerBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.function.Predicate;

public class SpawnerListeners implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        final Block block = e.getClickedBlock();
        if (block != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Location location = block.getLocation();
            final Player p = e.getPlayer();
            final UserDetails user = new UserDetails(p).query().persist();
            if (user.exists(location)) {
                e.setCancelled(true);
                final Spawner spawner = user.getSpawner(location);
                new InfoSpawnerMenu(spawner, p);
            } else {
                final SyncRequests<Spawner, String> q = new Spawner(location).query();
                if (!q.exists()) return;
                final Spawner spawner = q.persist();
                if (spawner.hasPermission(p.getName())) {
                    new InfoSpawnerMenu(spawner, p);
                } else p.sendMessage(Main.getMessages().getMessage("isNotOwner"));
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        final ItemStack item = e.getItemInHand();
        if (!new SpawnerItem().isSpawnerItem(item)) return;
        e.setCancelled(true);
        final Player p = e.getPlayer();
        final Location location = e.getBlock().getLocation();
        final SpawnerItem spawnerItem = new SpawnerItem().parse(item);
        final Predicate<ItemStack> prd = itemStack -> new SpawnerItem().isSpawnerItem(itemStack) && spawnerItem.isSimilar(itemStack);
        final DCached cached = DCached.get();
        if (cached.exists(s -> s.equalsIgnoreCase(p.getName()))) {
            p.sendMessage("§cAguarde para poder colocar o gerador novamente.");
            return;
        }
        final SpawnerItem spawnerItem1 = Arrays.stream(p.getInventory().getContents())
                .filter(prd)
                .map(itemStack -> new SpawnerItem().parse(itemStack))
                .reduce(SpawnerItem::concat).get();
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (prd.test(p.getInventory().getItem(i))) {
                p.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        }
        final Spawner spawner = new SpawnerBuilder(location, p.getName()).build(spawnerItem1);
        final UserDetails user = new UserDetails(p).query().persist();
        user.getSpawners(spawners -> {
            final FilteringFunctions functions = new FilteringFunctions(spawners);
            if (functions.exists(spawner.getEntityType())) {
                final Spawner spawner1 = functions.get(spawner.getEntityType());
                spawner1.concat(spawner);
                spawner1.query().commit();
            } else {
                user.addSpawnerLocation(location);
                spawner.appear(spawner1 -> {
                    user.query().commit();
                    spawner1.query().commit();
                    AnimationPlace.builder()
                            .ticksToFinalize(115)
                            .red(50)
                            .green(168)
                            .blue(82).build().send(spawner1);
                });
            }
        });
        cached.add(p.getName());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        final Block block = e.getBlock();
        final Location location = block.getLocation();
        final Player p = e.getPlayer();
        final Configuration messages = Main.getMessages();
        final UserDetails user = new UserDetails(p).query().persist();
        final DCached cached = DCached.get();
        if (user.exists(location)) {
            e.setCancelled(true);
            if (cached.exists(s -> s.equalsIgnoreCase(p.getName()))) {
                p.sendMessage("§cAguarde para poder retirar o gerador novamente.");
                return;
            }
            final Spawner spawner = user.getSpawner(location);
            new SpawnerItem().parse(spawner).giveItem(p);
            spawner.destroy(user);
            p.sendMessage(messages.getMessage("spawnerRemoved"));
            cached.add(p.getName());
            new AnimationBreak().send(spawner);
        } else if (new Spawner(location).query().exists()) {
            e.setCancelled(true);
            p.sendMessage(messages.getMessage("isNotOwner"));
        }
    }

}
