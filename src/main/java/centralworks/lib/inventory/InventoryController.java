package centralworks.lib.inventory;

import centralworks.Main;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.function.Consumer;

public class InventoryController implements Listener {

    static {
        instance = new InventoryController(Main.getInstance());
    }

    @Getter
    private static final InventoryController instance;

    @Getter
    private final HashMap<InventoryMaker, Consumer<InventoryClickEvent>> consumersOnClick = Maps.newHashMap();
    @Getter
    private final HashMap<InventoryMaker, Consumer<InventoryCloseEvent>> consumersOnClose = Maps.newHashMap();

    private final Main plugin;

    private InventoryController(Main main) {
        plugin = main;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        consumersOnClick.forEach((inventoryMaker, consumer) -> {
            final Inventory inventory = inventoryMaker.getInventory();
            if (consumer != null) {
                if (event.getClickedInventory() == null) return;
                if (!event.getWhoClicked().getOpenInventory().getTopInventory().equals(inventory)) return;
                if (event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
                    consumer.accept(event);
                }
            }
            if (inventory == null) return;
            if (inventory.equals(event.getInventory())) {
                if (inventoryMaker.isCancellable()) event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
                final Item item = inventoryMaker.getItems().get(event.getRawSlot());
                if (item == null) return;
                if (item.isCancellable()) event.setCancelled(true);
                if (item.getClickEventConsumer() != null) item.getClickEventConsumer().accept(event);
            }
        });
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        consumersOnClose.forEach((inventoryMaker, consumer) -> {
            if (!inventoryMaker.getInventory().equals(event.getInventory())) return;
            if (consumer != null) consumer.accept(event);
            if (inventoryMaker.isUpgradeable())
                inventoryMaker.getSchID().forEach(integer -> Bukkit.getScheduler().cancelTask(integer));
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                getConsumersOnClick().remove(inventoryMaker);
                getConsumersOnClose().remove(inventoryMaker);
            }, 5L);
        });
    }


}
