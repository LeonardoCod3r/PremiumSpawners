package centralworks.models;

import centralworks.Main;
import centralworks.spawners.Identifiers;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Settings;
import centralworks.lib.enums.EntityName;
import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtinjector.NBTInjector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class EntityStacked {

    @Getter
    @Setter
    private Entity entity;
    @Getter
    private Double stack;


    public EntityStacked(Entity entity) {
        this.entity = entity;
        apply().updateName();
    }

    public void setStack(Double stack) {
        this.stack = stack;
        NBTInjector.getNbtData(entity).setDouble(Identifiers.NBT_TAG_ENTITY_AMOUNT, getStack());
        updateName();
    }

    public void addStack(Double stack) {
        this.stack += stack;
        NBTInjector.getNbtData(entity).setDouble(Identifiers.NBT_TAG_ENTITY_AMOUNT, getStack());
        updateName();
    }

    public EntityStacked cloneEntity() {
        final Entity newEntity = entity.getLocation().getWorld().spawnEntity(entity.getLocation(), entity.getType());
        final EntityStacked entityStacked = new EntityStacked(newEntity);
        entityStacked.setStack(getStack());
        return entityStacked;
    }

    public void concat(EntityStacked entityStacked) {
        if (entityStacked.getEntity().isDead()) return;
        if (getEntity().getType() != entityStacked.getEntity().getType()) return;
        final Settings.Navigate entities = Main.getInstance().getEntities().navigate();
        final Double maxStack = entities.getDouble("Settings.max-stack");
        final Double amount = entityStacked.getStack();
        if (amount == 0.0) {
            entity.remove();
            return;
        }
        if (getStack() + amount > maxStack) {
            final double delta = stack + amount - maxStack;
            entityStacked.setStack(delta);
            setStack(maxStack);
        } else {
            addStack(amount);
            entityStacked.getEntity().remove();
        }
    }

    public void removeStack(Double stack) {
        this.stack -= stack;
        NBTInjector.getNbtData(entity).setDouble(Identifiers.NBT_TAG_ENTITY_AMOUNT, getStack());
        updateName();
    }

    private EntityStacked apply() {
        setEntity(NBTInjector.patchEntity(entity));
        final NBTCompound data = NBTInjector.getNbtData(entity);
        if (data.hasKey(Identifiers.NBT_TAG_ENTITY_AMOUNT))
            this.stack = data.getDouble(Identifiers.NBT_TAG_ENTITY_AMOUNT);
        else this.stack = 1.0;
        return this;
    }

    private EntityStacked updateName() {
        final Settings.Navigate configuration = Main.getInstance().getEntities().navigate();
        entity.setCustomName(configuration.getColorfulString("Settings.name").replace("{mob}", EntityName.valueOf(entity).getName()).replace("{stack}", BalanceFormatter.format(getStack())));
        entity.setCustomNameVisible(true);
        return this;
    }

    public EntityStacked noAI() {
        new NBTEntity(entity).setInteger("NoAI", 1);
        return this;
    }

    public Double getAmountDrops(ItemStack itemStack) {
        final double m = itemStack.containsEnchantment(Enchantment.LOOT_BONUS_MOBS) ? itemStack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1 : 1;
        return Math.ceil(/*getAmountDrops() or getStack()*/ getStack() * m);
    }

    public Double getAmountDrops(ItemStack itemStack, boolean isSneaking) {
        final double stack = isSneaking ? 1 : getStack();
        final double m = itemStack.containsEnchantment(Enchantment.LOOT_BONUS_MOBS) ? itemStack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1 : 1;
        return stack * m;
    }

    public List<ItemStack> getDrops(ItemStack itemStack, ItemStack drop, boolean isSneaking, Integer i, int max) {
        final List<ItemStack> itemStacks = Lists.newArrayList();
        i = Math.min(max, i == null ? Integer.parseInt(getAmountDrops(itemStack, isSneaking).toString().replace(".0", "")) : i);
        final int times = i / 64;
        final int rest = i % 64;
        for (int t = 0; t < times; t++) {
            final ItemStack it = drop.clone();
            it.setAmount(64);
            itemStacks.add(it);
        }
        if (rest > 0) {
            drop.setAmount(rest);
            itemStacks.add(drop);
        }
        return itemStacks;
    }

    public Double getAmountDrops() {
        return Math.ceil(getStack() * ThreadLocalRandom.current().nextInt(1, 3));
    }

}
