package centralworks.spawners.modules.models.quests.suppliers;

import centralworks.spawners.Main;
import centralworks.spawners.modules.models.quests.cached.Runnables;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Data
@RequiredArgsConstructor
public class CraftRewardRunnable implements CraftSliceReward<CraftRewardRunnable>{

    private String id;

    @Override
    public ItemStack getAsItem() {
        return null;
    }

    @Override
    public String getAsCommand() {
        return null;
    }

    @Override
    public Consumer<String> getAsRunnable() {
        return Runnables.get().get(qrr -> qrr.getId().equals(id)).getToReward();
    }

    @Override
    public void toAward(String operator) {
        getAsRunnable().accept(operator);
    }

    @Override
    public CraftRewardRunnable parse(JsonObject jsonObject) {
        return Main.getGson().fromJson(jsonObject, CraftRewardRunnable.class);
    }
}
