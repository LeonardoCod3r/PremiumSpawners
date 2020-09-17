package centralworks.spawners.modules.models.addons;

import centralworks.spawners.modules.models.ItemSettings;
import centralworks.spawners.modules.models.Settings;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Data
@RequiredArgsConstructor
public class Impulse {

    private String id;
    private String name;
    private Integer time;
    private ImpulseType type;
    private Double value;
    private String permission;
    private ItemSettings item;

    public ItemStack getAsItem() {
        final Settings settings = Settings.get();
        final ItemStack itemStack = item.getAsItem(s -> s);
        final NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setDouble(settings.getNBT_TAG_BOOSTER_VALUE(), value);
        nbtItem.setInteger(settings.getNBT_TAG_BOOSTER_DELAY(), time);
        nbtItem.setString(settings.getNBT_TAG_BOOSTER_TYPE(), type.name());
        return nbtItem.getItem();
    }

    public boolean hasPermission() {
        return permission.equalsIgnoreCase("");
    }

    public boolean hasPermission(Player p) {
        if (!hasPermission()) return true;
        return p.hasPermission(getPermission());
    }

}
