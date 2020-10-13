package centralworks.lib.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class InventoryMaker {

    @Getter
    private final Inventory inventory;
    @Getter
    private final HashMap<Integer, Item> items = Maps.newHashMap();
    private final HashMap<Integer, Character> pattern = Maps.newHashMap();
    @Getter
    private final List<Integer> schID = new ArrayList<>();
    @Getter
    private boolean cancellable = false;
    @Getter
    private boolean upgradeable = false;

    public InventoryMaker() {
        this.inventory = Bukkit.createInventory(null, 6, "");
    }

    public InventoryMaker(Integer rows, String name) {
        this.inventory = Bukkit.createInventory(null, rows * 9, name);
    }

    public void setCancellable(Boolean cancellable) {
        this.cancellable = cancellable;
    }

    public InventoryMaker setItem(Integer slot, Item item) {
        this.items.put(slot, item);
        this.inventory.setItem(slot, item.getItemStack());
        return this;
    }

    public InventoryMaker onClose(Consumer<InventoryCloseEvent> consumer) {
        final InventoryController controller = InventoryController.getInstance();
        final HashMap<InventoryMaker, Consumer<InventoryCloseEvent>> consumers = controller.getConsumersOnClose();
        if (consumers.containsKey(this)) consumers.replace(this, consumer);
        else consumers.put(this, consumer);
        return this;
    }

    public InventoryMaker onClick(Consumer<InventoryClickEvent> consumer) {
        final InventoryController controller = InventoryController.getInstance();
        final HashMap<InventoryMaker, Consumer<InventoryClickEvent>> consumers = controller.getConsumersOnClick();
        if (consumers.containsKey(this)) consumers.replace(this, consumer);
        else consumers.put(this, consumer);
        return this;
    }

    public InventoryMaker addItem(Item item) {
        int slot = this.items.size();
        this.items.put(slot, item);
        this.inventory.setItem(slot, item.getItemStack());
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
                inventory.setItem(integer, item.getItemStack());
                items.put(integer, item);
                slot++;
            }
        }
    }

    public void open(Player player) {
        if (this.inventory == null) return;
        final InventoryController controller = InventoryController.getInstance();
        if (!controller.getConsumersOnClick().containsKey(this)) controller.getConsumersOnClick().put(this, null);
        if (!controller.getConsumersOnClose().containsKey(this)) controller.getConsumersOnClose().put(this, null);
        organize();
        player.openInventory(this.inventory);
    }

    public void clear() {
        inventory.clear();
    }

}



