package centralworks.lib;

import centralworks.lib.inventory.Item;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class ItemSettings implements Cloneable{

    private String item_id;
    private Integer item_data;
    private String item_skull_owner;
    private String item_skull_url;
    private Integer item_amount;
    private Integer item_slot;
    private String item_name;
    private boolean hide;
    private List<String> item_lore;
    private List<String> item_enchants;

    @Override
    public ItemSettings clone() throws CloneNotSupportedException {
        return (ItemSettings) super.clone();
    }

    public ItemStack getAsItem(Function<String, String> loreReplacement) {
        if (hide) return new ItemStack(Material.AIR);
        final Item item = new Item(Material.getMaterial(item_id), item_amount, item_data.shortValue());
        item.name(item_name.replace("&", "§"));
        item.lore(item_lore.stream().map(s -> s.replace("&", "§")).map(loreReplacement).collect(Collectors.toList()));
        item.setSkullUrl(item_skull_url);
        item.setSkullOwner(item_skull_owner);
        item_enchants.forEach(s -> {
            final String[] split = s.split(":");
            item.enchant(Enchantment.getByName(split[0]), Integer.valueOf(split[1]));
        });
        return item.getItemStack();
    }

}
