package centralworks.listeners.stackmobs;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.enums.ImpulseType;
import centralworks.spawners.models.Spawner;
import centralworks.models.EntityStacked;
import centralworks.lib.LocationUtils;
import centralworks.lib.Settings;
import com.google.common.cache.LoadingCache;
import de.tr7zw.nbtinjector.NBTInjector;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class EntityListeners implements Listener {

    private final Main plugin;

    public EntityListeners() {
        this.plugin = Main.getInstance();
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (!e.getEntity().hasMetadata("NPC") && e.getEntity() instanceof Creature)
            NBTInjector.patchEntity(e.getEntity());
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent e) {
        final Settings.Navigate entities = plugin.getEntities().navigate();
        if (allowedWorld(e.getLocation().getWorld().getName())) return;
        final Entity entity = e.getEntity();
        if (!entity.hasMetadata("NPC") && entity instanceof Creature) {
            final EntityStacked entityStacked = new EntityStacked(entity).noAI();
            final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
            Optional.ofNullable(cache.getIfPresent(LocationUtils.locToString(e.getSpawner().getLocation())))
                    .ifPresent(spawner -> entityStacked.setStack(spawner.getAmount() * spawner.getMultiplierOf(ImpulseType.GENERATION)));
            for (Entity nearbyEntity : entity.getNearbyEntities(entities.getDouble("Settings.area.x"), entities.getDouble("Settings.area.y"), entities.getDouble("Settings.area.z"))) {
                if (!nearbyEntity.hasMetadata("NPC") && nearbyEntity instanceof Creature)
                    new EntityStacked(nearbyEntity).concat(entityStacked);
            }
        }
    }

    @EventHandler
    public void onSpawnCreature(CreatureSpawnEvent e) {
        if (allowedWorld(e.getLocation().getWorld().getName())) return;
        final CreatureSpawnEvent.SpawnReason reason = e.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.EGG || reason == CreatureSpawnEvent.SpawnReason.CUSTOM || reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }
        final Entity entity = e.getEntity();
        stackNearbyEntities(entity);
    }

    @EventHandler
    public void onLoad(ChunkLoadEvent e) {
        final Chunk chunk = e.getChunk();
        if (allowedWorld(chunk.getWorld().getName())) return;
        for (Entity entity : chunk.getEntities()) {
            stackNearbyEntities(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            final Player p = e.getEntity().getKiller();
            final Entity mob = e.getEntity();
            if (!plugin.dropStorageSystemIsActive()) {
                final boolean sneaking = p.isSneaking();
                final EntityStacked entityStacked = new EntityStacked(mob);
                final List<ItemStack> drops = entityStacked.getDrops(p.getItemInHand(), e.getDrops().get(0), sneaking, null, 1200000);
                e.getDrops().clear();
                e.getDrops().addAll(drops);
                if (sneaking) entityStacked.cloneEntity().removeStack(1.0);
            }
        }
    }

    private void stackNearbyEntities(Entity entity) {
        final Settings.Navigate entities = plugin.getEntities().navigate();
        if (!entity.hasMetadata("NPC") && entity instanceof Creature) {
            final EntityStacked entityStacked = new EntityStacked(entity).noAI();
            for (Entity nearbyEntity : entity.getNearbyEntities(entities.getDouble("Settings.area.x"), entities.getDouble("Settings.area.y"), entities.getDouble("Settings.area.z"))) {
                if (!nearbyEntity.hasMetadata("NPC") && nearbyEntity instanceof Creature)
                    new EntityStacked(nearbyEntity).concat(entityStacked);
            }
        }
    }

    private boolean allowedWorld(String name) {
        final Settings.Navigate entities = plugin.getEntities().navigate();
        return entities.getList("Settings.worldBlackList").contains(name);
    }
}
