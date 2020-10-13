package centralworks.core.stackmobs.listeners;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.commons.models.enums.ImpulseType;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.stackmobs.models.EntityStacked;
import centralworks.lib.Configuration;
import centralworks.lib.Utils;
import com.google.common.cache.LoadingCache;
import de.tr7zw.nbtinjector.NBTInjector;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EntityListeners implements Listener {

    private final List<EntityType> indeterminate;
    private final Main plugin;

    public EntityListeners() {
        this.indeterminate = Arrays.asList(EntityType.IRON_GOLEM, EntityType.VILLAGER, EntityType.SLIME, EntityType.MAGMA_CUBE);
        this.plugin = Main.getInstance();
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent e) {
        if (!e.getEntity().hasMetadata("NPC") && e.getEntity() instanceof Animals || e.getEntity() instanceof Monster || indeterminate.contains(e.getEntityType()))
            NBTInjector.patchEntity(e.getEntity());
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent e) {
        final Configuration entities = plugin.getEntities();
        if (entities.getList("Settings.worldBlackList", false).contains(e.getLocation().getWorld().getName())) return;
        final Entity entity = e.getEntity();
        if (!entity.hasMetadata("NPC") && entity instanceof Animals || entity instanceof Monster || indeterminate.contains(entity.getType())) {
            final EntityStacked entityStacked = new EntityStacked(entity).noAI();
            final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
            Optional.ofNullable(cache.getIfPresent(Utils.locToString(e.getSpawner().getLocation())))
                    .ifPresent(spawner -> entityStacked.setStack(spawner.getAmount() * spawner.getMultiplierOf(ImpulseType.GENERATION)));
            for (Entity nearbyEntity : entity.getNearbyEntities(entities.getDouble("Settings.area.x"), entities.getDouble("Settings.area.y"), entities.getDouble("Settings.area.z"))) {
                if (!nearbyEntity.hasMetadata("NPC") && nearbyEntity instanceof Animals || nearbyEntity instanceof Monster || indeterminate.contains(nearbyEntity.getType()))
                    new EntityStacked(nearbyEntity).concat(entityStacked);
            }
        }
    }

    @EventHandler
    public void onLoad(ChunkLoadEvent e) {
        final Chunk chunk = e.getChunk();
        final Configuration entities = plugin.getEntities();
        if (entities.getList("Settings.worldBlackList", false).contains(chunk.getWorld().getName())) return;
        for (Entity entity : chunk.getEntities()) {
            if (!entity.hasMetadata("NPC") && entity instanceof Animals || entity instanceof Monster || indeterminate.contains(entity.getType())) {
                final EntityStacked entityStacked = new EntityStacked(entity).noAI();
                for (Entity nearbyEntity : entity.getNearbyEntities(entities.getDouble("Settings.area.x"), entities.getDouble("Settings.area.y"), entities.getDouble("Settings.area.z"))) {
                    if (!nearbyEntity.hasMetadata("NPC") && nearbyEntity instanceof Animals || nearbyEntity instanceof Monster || indeterminate.contains(entity.getType()))
                        new EntityStacked(nearbyEntity).concat(entityStacked);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent e) {
        final Main plugin = Main.getInstance();
        if (e.getEntity().getKiller() != null) {
            final Player p = e.getEntity().getKiller();
            final Entity mob = e.getEntity();
            if (!plugin.dropStorageSystemIsActive()) {
                final EntityStacked entityStacked = new EntityStacked(mob);
                entityStacked.dropLoot(p.getItemInHand(), e.getDrops().get(0), 120000);
                e.getDrops().clear();
            }
        }
    }

}
