package centralworks.core.dropstorage.listeners;

import centralworks.Main;
import centralworks.core.dropstorage.cache.LootData;
import centralworks.core.dropstorage.models.Drop;
import centralworks.core.dropstorage.models.DropPlayer;
import centralworks.core.dropstorage.models.DropStorage;
import centralworks.lib.ActionBarMessage;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Configuration;
import centralworks.lib.enums.ItemName;
import centralworks.core.stackmobs.models.EntityStacked;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent e) {
        final Main plugin = Main.getInstance();
        if (e.getEntity().getKiller() != null) {
            final Player p = e.getEntity().getKiller();
            final Entity mob = e.getEntity();
            if (plugin.dropStorageSystemIsActive()) {
                if (plugin.getDropStorage().getList("Settings.black-list", false).contains(e.getEntity().getWorld().getName()))
                    return;
                final DropStorage dropStorage = new DropStorage(p).query().persist();
                for (DropPlayer dropPlayer : dropStorage.getDropPlayers()) {
                    final Drop drop = LootData.get().get(dropPlayer.getKeyDrop());
                    if (drop.getEntityType().equals(mob.getType())) {
                        final Configuration messages = plugin.getMessages();
                        if (!dropStorage.isMax()) {
                            final Double add = Math.ceil(new EntityStacked(mob).getAmountDrops(p.getItemInHand()) * dropStorage.getAllMultipliers());
                            if (dropStorage.isMax(add))
                                dropPlayer.addDropAmount(add - (dropStorage.getAmountAll() + add - dropStorage.getUser().getSellLimit()));
                            else dropPlayer.addDropAmount(add);
                            new ActionBarMessage(p, messages.getMessage("drops-add").replace("{amount}", BalanceFormatter.format(add)).replace("{drop-type}", ItemName.valueOf(drop.getDrop()).getName()));
                        } else new ActionBarMessage(p, messages.getMessage("armazem-max"));
                        if (dropStorage.isAutoSell()) {
                            if (dropPlayer.getAmount() > 0) dropPlayer.sell(p.getPlayer(), dropStorage);
                        }
                        dropStorage.query().commit();
                        e.getDrops().clear();
                        return;
                    }
                }
            }
        }
    }

}
