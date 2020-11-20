package centralworks.core.spawners.models;

import centralworks.Main;
import centralworks.core.spawners.Settings;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Configuration;
import centralworks.lib.enums.EntityName;
import centralworks.lib.inventory.Item;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class SpawnerItem implements Cloneable {

    private int id;
    private short data;
    private String name = "";
    private List<String> lore = Lists.newArrayList();
    private String skullUrl = "";
    private String skullOwner = "";
    private EntityType entityType;
    private int amountItem = 1;
    private Double amountSpawners = 1.0;

    public ItemStack getItem() {
        final Item item = new Item(new ItemStack(id, amountItem, data));
        final ItemStack itemStack = item.name(name).lore(
                lore.stream().map(s -> s
                        .replace("{entity-type}", EntityName.valueOf(entityType).getName())
                        .replace("{stack}", BalanceFormatter.format(amountSpawners))
                ).collect(Collectors.toList())
        ).setSkullOwner(skullOwner).setSkullUrl(skullUrl).getItemStack();
        final net.minecraft.server.v1_8_R3.ItemStack copy = CraftItemStack.asNMSCopy(itemStack);
        final NBTTagCompound tag = copy.getTag();
        tag.setDouble(Settings.NBT_TAG_SPAWNER_AMOUNT, amountSpawners);
        tag.setString(Settings.NBT_TAG_ENTITY_TYPE, entityType.toString());
        copy.setTag(tag);
        return CraftItemStack.asBukkitCopy(copy);
    }

    @Override
    public SpawnerItem clone() {
        try {
            return (SpawnerItem) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }

    public List<ItemStack> getItems() {
        final List<ItemStack> list = Lists.newArrayList();
        final int v = amountItem / 64;
        final int rest = amountItem % 64;
        if (v != 0) {
            for (int i = v; i != 0; i--) {
                final SpawnerItem clone = this.clone();
                clone.setAmountItem(64);
                list.add(clone.getItem());
            }
        }
        if (rest != 0) {
            final SpawnerItem clone = this.clone();
            clone.setAmountItem(rest);
            list.add(clone.getItem());
        }
        return list;
    }

    public boolean isSimilar(SpawnerItem spawnerItem) {
        return spawnerItem.getEntityType() == getEntityType() && spawnerItem.getAmountSpawners().equals(getAmountSpawners());
    }

    public boolean isSimilar(ItemStack item) {
        final SpawnerItem spawnerItem = new SpawnerItem().parse(item);
        return spawnerItem.getEntityType() == getEntityType() && spawnerItem.getAmountSpawners().equals(getAmountSpawners());
    }

    public boolean typeOf(SpawnerItem spawnerItem) {
        return spawnerItem.getEntityType() == getEntityType();
    }

    public void giveItem(Player p) {
        final ItemStack itemStack = getItem();
        final int stackMax = itemStack.getMaxStackSize();
        int amount = itemStack.getAmount();
        for (ItemStack itemStack1 : p.getInventory().getContents()) {
            if (amount == 0) return;
            if (itemStack1 == null || !isSpawnerItem(itemStack1)) continue;
            if (isSimilar(itemStack1) && itemStack1.getAmount() < stackMax) {
                if (itemStack1.getAmount() + amount <= stackMax) {
                    itemStack1.setAmount(itemStack1.getAmount() + amount);
                    return;
                }
                amount = amount + itemStack1.getAmount() - stackMax;
                itemStack1.setAmount(stackMax);
            }
        }
        if (amount == 0) return;
        if (amount <= stackMax) {
            itemStack.setAmount(amount);
            if (p.getInventory().firstEmpty() != -1) p.getInventory().addItem(itemStack);
            else p.getWorld().dropItem(p.getLocation(), itemStack);
            return;
        }
        final int items = amount / stackMax;
        final int rest = amount % stackMax;
        final ItemStack newItemStack = itemStack.clone();
        newItemStack.setAmount(rest);
        if (p.getInventory().firstEmpty() != -1) p.getInventory().addItem(newItemStack);
        else p.getWorld().dropItem(p.getLocation(), newItemStack);
        for (int item = items; item > 0; item--) {
            final ItemStack newItem = itemStack.clone();
            newItem.setAmount(stackMax);
            if (p.getInventory().firstEmpty() != -1) p.getInventory().addItem(newItem);
            else p.getWorld().dropItem(p.getLocation(), newItem);
        }
    }

    public boolean isSpawnerItem(ItemStack itemStack) {
        try {
            return CraftItemStack.asNMSCopy(itemStack).getTag().hasKey(Settings.NBT_TAG_ENTITY_TYPE);
        } catch (Exception e) {
            return false;
        }
    }

    public SpawnerItem concat(ItemStack itemStack) {
        if (isSpawnerItem(itemStack)) {
            final SpawnerItem item = new SpawnerItem().parse(itemStack);
            if (item.getEntityType() != entityType) return this;
            setAmountItem(getAmountItem() + item.getAmountItem());
        }
        return this;
    }

    public SpawnerItem concat(SpawnerItem item) {
        if (item.getEntityType() != entityType) return this;
        setAmountItem(getAmountItem() + item.getAmountItem());
        return this;
    }

    public SpawnerItem getTogether() {
        setAmountSpawners(getAmountSpawners() * getAmountItem());
        setAmountItem(1);
        return this;
    }

    public SpawnerItem parse(EntityType entityType) {
        final Configuration configuration = Main.getInstance().getSpawners();
        final String path = "List." + entityType.toString() + ".item.";
        setEntityType(entityType);
        setId(configuration.getInt(path + "id"));
        setData(configuration.getInt(path + "data").shortValue());
        setSkullUrl(configuration.get(path + "skull-url", false));
        setSkullOwner(configuration.get(path + "skull-owner", false));
        setName(configuration.get(path + "name", true));
        setLore(configuration.getList(path + "lore", true));
        setAmountItem(1);
        setAmountSpawners(1.0);
        return this;
    }

    public SpawnerItem parse(Spawner spawner) {
        final Configuration configuration = Main.getInstance().getSpawners();
        final String path = "List." + spawner.getEntityType().toString() + ".item.";
        setEntityType(spawner.getEntityType());
        setId(configuration.getInt(path + "id"));
        setData(configuration.getInt(path + "data").shortValue());
        setSkullUrl(configuration.get(path + "skull-url", false));
        setSkullOwner(configuration.get(path + "skull-owner", false));
        setName(configuration.get(path + "name", true));
        setLore(configuration.getList(path + "lore", true));
        setAmountItem(1);
        setAmountSpawners(spawner.getAmount());
        return this;
    }

    public SpawnerItem parse(ItemStack itemStack) {
        try {
            final net.minecraft.server.v1_8_R3.ItemStack copy = CraftItemStack.asNMSCopy(itemStack);
            final NBTTagCompound nbt = copy.getTag();
            setId(itemStack.getTypeId());
            setData(itemStack.getDurability());
            if (itemStack.getItemMeta() instanceof SkullMeta) {
                final SkullMeta meta = ((SkullMeta) itemStack.getItemMeta());
                setSkullOwner(meta.getOwner());
                setSkullUrl(new Item(itemStack).getSkullUrl());
            }
            setName(itemStack.getItemMeta().getDisplayName());
            setLore(itemStack.getItemMeta().getLore());
            setAmountItem(itemStack.getAmount());
            setAmountSpawners(nbt.getDouble(Settings.NBT_TAG_SPAWNER_AMOUNT));
            setEntityType(EntityType.valueOf(nbt.getString(Settings.NBT_TAG_ENTITY_TYPE)));
        } catch (Exception ignored) {
        }
        return this;
    }

}
