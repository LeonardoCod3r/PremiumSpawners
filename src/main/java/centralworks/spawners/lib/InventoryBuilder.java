package centralworks.spawners.lib;

import centralworks.spawners.Main;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class InventoryBuilder implements Listener {

    private final Inventory inventory;
    private final HashMap<Integer, Item> items = Maps.newHashMap();
    private final HashMap<Integer, Character> pattern = Maps.newHashMap();
    private final List<Integer> schID = new ArrayList<>();
    private Boolean cancellable = false;
    private Boolean upgradeable = false;
    private Consumer<InventoryClickEvent> clickEventConsumer;
    private Consumer<InventoryCloseEvent> closeEventConsumer;

    public InventoryBuilder() {
        this.inventory = Bukkit.createInventory(null, 6, "");
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    public InventoryBuilder(Plugin plugin, Integer rows, String name) {
        this.inventory = Bukkit.createInventory(null, rows * 9, name);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void setCancellable(Boolean cancellable) {
        this.cancellable = cancellable;
    }

    public InventoryBuilder setItem(Integer slot, Item item) {
        this.items.put(slot, item);
        this.inventory.setItem(slot, item.build());
        return this;
    }

    public InventoryBuilder onClose(Consumer<InventoryCloseEvent> consumer) {
        this.closeEventConsumer = consumer;
        return this;
    }

    private Consumer<InventoryCloseEvent> getCloseEventConsumer() {
        return closeEventConsumer;
    }

    public InventoryBuilder addItem(Item item) {
        int slot = this.items.size();
        this.items.put(slot, item);
        this.inventory.setItem(slot, item.build());
        return this;
    }

    public void setUpgradeable(Boolean upgradeable) {
        this.upgradeable = upgradeable;
    }

    public void addScheduler(Integer id) {
        schID.add(id);
    }

    public Item getItem(Integer slot) {
        return this.items.get(slot);
    }

    public void setDesign(String... design) {
        String[] strings = design;
        Integer slot = 0;
        for (String current : strings) {
            char[] charArray;
            for (int length = (charArray = current.toCharArray()).length, i = 0; i < length; ++i) {
                char letter = charArray[i];
                pattern.put(slot, letter);
                slot++;
            }
        }
    }

    private void organize() {
        if (pattern.isEmpty()) return;
        HashMap<Integer, Item> cloned = Maps.newHashMap();
        cloned.putAll(items);
        List<Item> items1 = Lists.newArrayList();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i > pattern.size()) break;
            Item item = cloned.get(i);
            if (item != null && item.isEditable()) {
                inventory.setItem(i, null);
                items.remove(i);
                items1.add(item);
            }
        }
        int slot = 0;
        for (Integer integer : pattern.keySet()) {
            char character = pattern.get(integer);
            if (character != 'X') {
                if (slot >= items1.size()) break;
                Item item = items1.get(slot);
                inventory.setItem(integer, item.build());
                items.put(integer, item);
                slot++;
            }
        }
    }

    public void open(Player player) {
        if (this.inventory == null) return;
        organize();
        player.openInventory(this.inventory);
    }

    public void clear() {
        inventory.clear();
    }

    public InventoryBuilder onClickPlayerInv(Consumer<InventoryClickEvent> consumer) {
        this.clickEventConsumer = consumer;
        return this;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (clickEventConsumer != null) {
            if (event.getClickedInventory() == null) return;
            if (!event.getWhoClicked().getOpenInventory().getTopInventory().equals(inventory)) return;
            if (event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
                clickEventConsumer.accept(event);
            }
        }
        if (this.inventory == null) return;
        if (this.inventory.equals(event.getInventory())) {
            if (this.cancellable) event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
            Item item = this.items.get(event.getRawSlot());
            if (item == null) return;
            if (item.isCancellable()) event.setCancelled(true);
            if (item.getClickEventConsumer() != null) item.getClickEventConsumer().accept(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (this.inventory == null) return;
        if (this.upgradeable == null) return;
        schID.forEach(integer -> Bukkit.getScheduler().cancelTask(integer));
    }

    @EventHandler
    public void onClose2(InventoryCloseEvent e) {
        if (getCloseEventConsumer() != null) getCloseEventConsumer().accept(e);
    }
}