package centralworks.core.quests.other;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface CraftSliceReward<T> {

    ItemStack getAsItem();

    String getAsCommand();

    Consumer<String> getAsRunnable();

    void toAward(String operator);

    T parse(JsonObject jsonObject);


}
