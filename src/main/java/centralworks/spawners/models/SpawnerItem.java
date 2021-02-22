package centralworks.spawners.models;

import centralworks.Main;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.PlayerCommons;
import centralworks.lib.enums.EntityName;
import centralworks.lib.inventory.Item;
import centralworks.spawners.Identifiers;
import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
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
public class SpawnerItem {

    private String id;
    private short data;
    private String name = "";
    private List<String> lore = Lists.newArrayList();
    private String skullUrl = "";
    private String skullOwner = "";
    private EntityType entityType;
    private int amountItem = 1;
    private Double amountSpawners = 1.0;

    public SpawnerItem(SpawnerItem spawnerItem) {
        this.id = spawnerItem.getId();
        this.amountItem = spawnerItem.getAmountItem();
        this.amountSpawners = spawnerItem.getAmountSpawners();
        this.data = spawnerItem.getData();
        this.entityType = spawnerItem.getEntityType();
        this.name = spawnerItem.getName();
        this.lore = spawnerItem.getLore();
        this.skullOwner = spawnerItem.getSkullOwner();
        this.skullUrl = spawnerItem.getSkullUrl();
    }

    public ItemStack getItem() {
        val item = new Item(new ItemStack(Material.getMaterial(id), amountItem, data));
        val itemStack = item.name(name).lore(
                lore.stream().map(s -> s
                        .replace("{entity-type}", EntityName.valueOf(entityType).getName())
                        .replace("{stack}", BalanceFormatter.format(amountSpawners))
                ).collect(Collectors.toList())
        ).setSkullOwner(skullOwner).setSkullUrl(skullUrl).getItemStack();
        val copy = CraftItemStack.asNMSCopy(itemStack);
        val tag = copy.getTag();
        tag.setDouble(Identifiers.NBT_TAG_SPAWNER_AMOUNT, amountSpawners);
        tag.setString(Identifiers.NBT_TAG_ENTITY_TYPE, entityType.toString());
        copy.setTag(tag);
        return CraftItemStack.asBukkitCopy(copy);
    }

    public List<ItemStack> getItems() {
        final List<ItemStack> list = Lists.newArrayList();
        val v = amountItem / 64;
        val rest = amountItem % 64;
        val clone = new SpawnerItem(this);
        if (v != 0) {
            for (int i = v; i != 0; i--) {
                clone.setAmountItem(64);
                list.add(clone.getItem());
            }
        }
        if (rest != 0) {
            clone.setAmountItem(rest);
            list.add(clone.getItem());
        }
        return list;
    }

    public boolean isSimilar(SpawnerItem spawnerItem) {
        return spawnerItem.getEntityType() == getEntityType() && spawnerItem.getAmountSpawners().equals(getAmountSpawners());
    }

    public boolean typeOf(SpawnerItem spawnerItem) {
        return spawnerItem.getEntityType() == getEntityType();
    }

    public void giveItem(Player p) {
        val itemStack = getItem();
        val stackMax = itemStack.getMaxStackSize();
        var amount = itemStack.getAmount();
        for (ItemStack itemStack1 : p.getInventory().getContents()) {
            if (amount == 0) return;
            if (itemStack1 == null || !isSpawnerItem(itemStack1)) continue;
            if (isSimilar(new SpawnerItem(itemStack1)) && itemStack1.getAmount() < stackMax) {
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
        PlayerCommons.give(itemStack, amount, stackMax, p);
    }

    public static boolean isSpawnerItem(ItemStack itemStack) {
        try {
            return CraftItemStack.asNMSCopy(itemStack).getTag().hasKey(Identifiers.NBT_TAG_ENTITY_TYPE);
        } catch (Exception e) {
            return false;
        }
    }

    public SpawnerItem concat(ItemStack itemStack) {
        if (isSpawnerItem(itemStack)) {
            val item = new SpawnerItem(itemStack);
            if (item.getEntityType() != entityType) return this;
            this.amountItem = getAmountItem() + item.getAmountItem();
        }
        return this;
    }

    public SpawnerItem concat(SpawnerItem item) {
        if (item.getEntityType() != entityType) return this;
        this.amountItem = getAmountItem() + item.getAmountItem();
        return this;
    }

    public SpawnerItem getTogether() {
        setAmountSpawners(getAmountSpawners() * getAmountItem());
        setAmountItem(1);
        return this;
    }

    public SpawnerItem(EntityType entityType) {
        val configuration = Main.getInstance().getSpawners().navigate();
        val path = "List." + entityType.toString() + ".item.";
        this.entityType = entityType;
        this.id = configuration.getString(path + "id");
        this.data = configuration.getInt(path + "data").shortValue();
        this.skullUrl = configuration.getString(path + "skull-url");
        this.skullOwner = configuration.getString(path + "skull-owner");
        this.name = configuration.getColorfulString(path + "name");
        this.lore = configuration.getColorfulList(path + "lore");
        this.amountItem = 1;
        this.amountSpawners = 1.0;
    }

    public SpawnerItem(Spawner spawner) {
        val configuration = Main.getInstance().getSpawners().navigate();
        val path = "List." + spawner.getEntityType().toString() + ".item.";
        this.entityType = spawner.getEntityType();
        this.id = configuration.getString(path + "id");
        this.data = configuration.getInt(path + "data").shortValue();
        this.skullUrl = configuration.getString(path + "skull-url");
        this.skullOwner = configuration.getString(path + "skull-owner");
        this.name = configuration.getColorfulString(path + "name");
        this.lore = configuration.getColorfulList(path + "lore");
        this.amountItem = 1;
        this.amountSpawners = spawner.getAmount();
    }

    public void put(Location loc) {
        loc.getBlock().setType(Material.getMaterial("MOB_SPAWNER"));
        val spawnerBlock = ((CreatureSpawner) loc.getBlock().getState());
        spawnerBlock.setCreatureTypeByName(getEntityType().name());
        spawnerBlock.setSpawnedType(getEntityType());
        spawnerBlock.setDelay(20);
        spawnerBlock.update();
    }

    public SpawnerItem(ItemStack itemStack) {
        try {
            val copy = CraftItemStack.asNMSCopy(itemStack);
            val nbt = copy.getTag();
            this.id = itemStack.getType().name();
            this.data = itemStack.getDurability();
            if (itemStack.getItemMeta() instanceof SkullMeta) {
                val meta = ((SkullMeta) itemStack.getItemMeta());
                this.skullOwner = meta.getOwner();
                this.skullUrl = new Item(itemStack).getSkullUrl();
            }
            this.name = itemStack.getItemMeta().getDisplayName();
            this.lore = itemStack.getItemMeta().getLore();
            this.amountItem = itemStack.getAmount();
            this.amountSpawners = nbt.getDouble(Identifiers.NBT_TAG_SPAWNER_AMOUNT);
            this.entityType = EntityType.valueOf(nbt.getString(Identifiers.NBT_TAG_ENTITY_TYPE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
