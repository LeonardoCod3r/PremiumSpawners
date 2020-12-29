package centralworks.listeners.dropstorage;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.market.models.Product;
import centralworks.models.UserProduct;
import centralworks.models.ProductStorage;
import centralworks.models.EntityStacked;
import centralworks.lib.ActionBarMessage;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Settings;
import centralworks.lib.enums.ItemName;
import com.google.common.cache.LoadingCache;
import lombok.val;
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
                if (plugin.getDropStorage().navigate().getList("Settings.black-list").contains(e.getEntity().getWorld().getName()))
                    return;
                final LoadingCache<String, ProductStorage> cache = Caches.getCache(ProductStorage.class);
                final ProductStorage productStorage = cache.getIfPresent(p.getName());
                for (UserProduct userProduct : productStorage.getUserProducts()) {
                    val product = userProduct.getProduct().orElse(new Product());
                    if (product.getEntityType().equals(mob.getType())) {
                        final Settings.Navigate nav = plugin.getMessages().navigate();
                        if (!productStorage.isMax()) {
                            final Double add = Math.ceil(new EntityStacked(mob).getAmountDrops(p.getItemInHand()) * productStorage.getAllMultipliers());
                            if (productStorage.isMax(add))
                                userProduct.addDropAmount(add - (productStorage.getAmountAll() + add - productStorage.getUser().getSellLimit()));
                            else userProduct.addDropAmount(add);
                            new ActionBarMessage(p, nav.getMessage("drops-add").replace("{amount}", BalanceFormatter.format(add)).replace("{drop-type}", ItemName.valueOf(product.getId()).getName()));
                        } else new ActionBarMessage(p, nav.getMessage("armazem-max"));
                        if (productStorage.isAutoSell()) {
                            if (userProduct.getAmount() > 0) userProduct.sell(p.getPlayer(), productStorage);
                        }
                        e.getDrops().clear();
                        return;
                    }
                }
            }
        }
    }

}
