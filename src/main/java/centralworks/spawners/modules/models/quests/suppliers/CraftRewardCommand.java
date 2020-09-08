package centralworks.spawners.modules.models.quests.suppliers;

import centralworks.spawners.Main;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Data
@RequiredArgsConstructor
public class CraftRewardCommand implements CraftSliceReward<CraftRewardCommand>{

    private String command;

    @Override
    public ItemStack getAsItem() {
        return null;
    }

    @Override
    public String getAsCommand() {
        return command;
    }

    @Override
    public Consumer<String> getAsRunnable() {
        return null;
    }

    @Override
    public void toAward(String operator) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", operator));
    }

    @Override
    public CraftRewardCommand parse(JsonObject jsonObject) {
        return Main.getGson().fromJson(jsonObject, CraftRewardCommand.class);
    }
}

