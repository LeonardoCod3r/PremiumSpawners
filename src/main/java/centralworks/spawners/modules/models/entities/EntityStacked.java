package centralworks.spawners.modules.models.entities;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.enums.EntityName;
import centralworks.spawners.lib.BalanceFormatter;
import centralworks.spawners.modules.models.Settings;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtinjector.NBTInjector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

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
        NBTInjector.getNbtData(entity).setDouble(Settings.get().getNBT_TAG_ENTITY_AMOUNT(), getStack());
        updateName();
    }

    public void addStack(Double stack) {
        this.stack += stack;
        NBTInjector.getNbtData(entity).setDouble(Settings.get().getNBT_TAG_ENTITY_AMOUNT(), getStack());
        updateName();
    }

    public void concat(EntityStacked entityStacked) {
        if (entityStacked.getEntity().isDead()) return;
        if (getEntity().getType() != entityStacked.getEntity().getType()) return;
        final Configuration entities = Main.getInstance().getEntities();
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
        NBTInjector.getNbtData(entity).setDouble(Settings.get().getNBT_TAG_ENTITY_AMOUNT(), getStack());
        updateName();
    }

    public EntityStacked apply() {
        setEntity(NBTInjector.patchEntity(entity));
        final Settings se = Settings.get();
        final NBTCompound data = NBTInjector.getNbtData(entity);
        if (data.hasKey(se.getNBT_TAG_ENTITY_AMOUNT())) this.stack = data.getDouble(se.getNBT_TAG_ENTITY_AMOUNT());
        else this.stack = 1.0;
        return this;
    }

    public EntityStacked updateName() {
        final Configuration configuration = Main.getInstance().getEntities();
        entity.setCustomName(configuration.get("Settings.name", true).replace("{mob}", EntityName.valueOf(entity).getName()).replace("{stack}", BalanceFormatter.format(getStack())));
        entity.setCustomNameVisible(true);
        return this;
    }

    public EntityStacked noAI() {
        new NBTEntity(entity).setInteger("NoAI", 1);
        return this;
    }

    public Double getAmountDrops(ItemStack itemStack) {
        final double m = itemStack.containsEnchantment(Enchantment.LOOT_BONUS_MOBS) ? itemStack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) + 1 : 1;
        return Math.ceil(/*getAmountDrops()*/ getStack() * m);
    }

    public Double getAmountDrops() {
        return Math.ceil(getStack() * ThreadLocalRandom.current().nextInt(1, 3));
    }

    public void dropLoot(ItemStack tool, ItemStack loot, Integer max) {
        final int i = (int) Math.min(max, getAmountDrops(tool));
        final int times = i / 64;
        final int rest = i % 64;
        for (int t = 0; t < times; t++) {
            final ItemStack it = loot.clone();
            it.setAmount(64);
            entity.getLocation().getWorld().dropItem(entity.getLocation(), it);
        }
        if (rest > 0) {
            loot.setAmount(rest);
            entity.getLocation().getWorld().dropItem(entity.getLocation(), loot);
        }
    }

}
