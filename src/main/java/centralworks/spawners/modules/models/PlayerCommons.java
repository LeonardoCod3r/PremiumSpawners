package centralworks.spawners.modules.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Data
@RequiredArgsConstructor
public class PlayerCommons {

    private Player p;

    public PlayerCommons(Player p) {
        this.p = p;
    }

    public PlayerCommons giveItem(final ItemStack itemStack, Integer amount) {
        final int stackMax = itemStack.getMaxStackSize();
        for (ItemStack itemStack1 : p.getInventory().getContents()) {
            if (amount == 0) return this;
            if (itemStack1 == null) continue;
            if (itemStack.isSimilar(itemStack1) && itemStack1.getAmount() < stackMax) {
                if (itemStack1.getAmount() + amount <= stackMax) {
                    itemStack1.setAmount(itemStack1.getAmount() + amount);
                    return this;
                }
                amount = amount + itemStack1.getAmount() - stackMax;
                itemStack1.setAmount(stackMax);
            }
        }
        if (amount == 0) return this;
        if (amount <= stackMax) {
            itemStack.setAmount(amount);
            if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(itemStack);
            } else p.getWorld().dropItem(p.getLocation(), itemStack);
            return this;
        }
        final int items = amount / stackMax;
        final int rest = amount % stackMax;
        final ItemStack newItemStack = itemStack.clone();
        newItemStack.setAmount(rest);
        if (p.getInventory().firstEmpty() != -1) {
            p.getInventory().addItem(newItemStack);
        } else p.getWorld().dropItem(p.getLocation(), newItemStack);
        for (int item = items; item > 0; item--) {
            final ItemStack newItem = itemStack.clone();
            newItem.setAmount(stackMax);
            if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(newItem);
            } else p.getWorld().dropItem(p.getLocation(), newItem);
        }
        return this;
    }

    public boolean haveSpace(final ItemStack itemStack, Integer amount) {
        final int stackMax = itemStack.getMaxStackSize();
        for (ItemStack itemStack1 : p.getInventory().getContents()) {
            if (itemStack1 == null) {
                amount -= stackMax;
                continue;
            }
            if (itemStack1.isSimilar(itemStack)) {
                amount -= stackMax - itemStack1.getAmount();
            }
        }
        return amount <= 0;
    }

}
