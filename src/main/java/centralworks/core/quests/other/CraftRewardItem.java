package centralworks.core.quests.other;

import centralworks.lib.inventory.Item;
import centralworks.lib.PlayerCommons;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class CraftRewardItem implements CraftSliceReward<CraftRewardItem> {

    private Integer item_id;
    private String item_skull_owner;
    private String item_skull_url;
    private Integer item_amount;
    private Integer item_data;
    private String item_name;
    private List<String> item_enchants;
    private List<String> item_lore;

    @Override
    public ItemStack getAsItem() {
        final Item item = new Item(Material.getMaterial(item_id), item_amount, item_data.shortValue());
        item.name(item_name.replace("&", "§"));
        item.lore(item_lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList()));
        item.setSkullUrl(item_skull_url);
        item.setSkullOwner(item_skull_owner);
        item_enchants.forEach(s -> {
            final String[] split = s.split(":");
            item.enchant(Enchantment.getByName(split[0]), Integer.valueOf(split[1]));
        });
        return item.getItemStack();
    }

    @Override
    public String getAsCommand() {
        return null;
    }

    @Override
    public Consumer<String> getAsRunnable() {
        return null;
    }

    @Override
    public void toAward(String operator) {
        new PlayerCommons(Bukkit.getPlayer(operator)).giveItem(getAsItem(), 1);
    }

    @Override
    public CraftRewardItem parse(JsonObject jsonObject) {
        return new Gson().fromJson(jsonObject, CraftRewardItem.class);
    }
}
