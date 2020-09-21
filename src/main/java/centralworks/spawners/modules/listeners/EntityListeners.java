package centralworks.spawners.modules.listeners;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.lib.ItemName;
import centralworks.spawners.modules.models.ActionBarMessage;
import centralworks.spawners.modules.models.addons.ImpulseType;
import centralworks.spawners.modules.models.dropsstorage.DropPlayer;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;
import centralworks.spawners.modules.models.dropsstorage.supliers.Drop;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import centralworks.spawners.modules.models.entities.EntityStacked;
import centralworks.spawners.modules.models.spawners.Spawner;
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

public class EntityListeners implements Listener {

    private final List<EntityType> indeterminate;

    public EntityListeners() {
        this.indeterminate = Arrays.asList(EntityType.IRON_GOLEM, EntityType.VILLAGER, EntityType.SLIME, EntityType.MAGMA_CUBE);
    }

    @EventHandler
    public void spawn(EntitySpawnEvent e) {
        if (!e.getEntity().hasMetadata("NPC") && e.getEntity() instanceof Animals || e.getEntity() instanceof Monster || indeterminate.contains(e.getEntityType()))
            NBTInjector.patchEntity(e.getEntity());
    }

    @EventHandler
    public void spawn(SpawnerSpawnEvent e) {
        final Configuration entities = Main.getEntities();
        if (entities.getList("Settings.worldBlackList", false).contains(e.getLocation().getWorld().getName())) return;
        final Entity entity = e.getEntity();
        if (!entity.hasMetadata("NPC") && entity instanceof Animals || entity instanceof Monster || indeterminate.contains(entity.getType())) {
            final EntityStacked entityStacked = new EntityStacked(entity).noAI();
            new Spawner(e.getSpawner().getLocation()).query().ifExists(spawner -> entityStacked.setStack(spawner.getAmount() * spawner.getMultiplierOf(ImpulseType.GENERATION)));
            for (Entity nearbyEntity : entity.getNearbyEntities(entities.getDouble("Settings.area.x"), entities.getDouble("Settings.area.y"), entities.getDouble("Settings.area.z"))) {
                if (!nearbyEntity.hasMetadata("NPC") && nearbyEntity instanceof Animals || nearbyEntity instanceof Monster || indeterminate.contains(nearbyEntity.getType()))
                    new EntityStacked(nearbyEntity).concat(entityStacked);
            }
        }
    }

    @EventHandler
    public void chunkLoadEvent(ChunkLoadEvent e) {
        final Chunk chunk = e.getChunk();
        final Configuration entities = Main.getEntities();
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
    public void kill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            final Player p = e.getEntity().getKiller();
            final Entity mob = e.getEntity();
            if (Main.get().dropStorageSystemIsActive()) {
                if (Main.getDropStorage().getList("Settings.black-list", false).contains(e.getEntity().getWorld().getName()))
                    return;
                final DropStorage dropStorage = new DropStorage(p).query().persist();
                for (DropPlayer dropPlayer : dropStorage.getDropPlayers()) {
                    final Drop drop = LootData.get().get(dropPlayer.getKeyDrop());
                    if (drop.getEntityType().equals(mob.getType())) {
                        final Configuration messages = Main.getMessages();
                        if (!dropStorage.isMax()) {
                            final Double add = Math.floor(new EntityStacked(mob).getAmountDrops(p.getItemInHand()) * dropStorage.getAllMultipliers());
                            if (dropStorage.isMax(add))
                                dropPlayer.addDropAmount(add - (dropStorage.getAmountAll() + add - dropStorage.getUser().getSellLimit()));
                            else dropPlayer.addDropAmount(add);
                            new ActionBarMessage(p, messages.getMessage("drops-add").replace("{amount}", FormatBalance.format(add)).replace("{drop-type}", ItemName.valueOf(drop.getDrop()).getName()));
                        } else new ActionBarMessage(p, messages.getMessage("armazem-max"));
                        if (dropStorage.isAutoSell()) {
                            if (dropPlayer.getAmount() > 0) dropPlayer.sell(p.getPlayer(), dropStorage);
                        }
                        dropStorage.query().commit();
                        e.getDrops().clear();
                        return;
                    }
                }
            } else {
                final EntityStacked entityStacked = new EntityStacked(mob);
                entityStacked.dropLoot(p.getItemInHand(), e.getDrops().get(0), 120000);
                e.getDrops().clear();
            }
        }
    }
}
