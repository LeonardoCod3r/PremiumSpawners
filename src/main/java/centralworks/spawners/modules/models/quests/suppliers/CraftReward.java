package centralworks.spawners.modules.models.quests.suppliers;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CraftReward {

    private String id;
    private CraftRewardType type;
    private JsonObject sliceReward;

    public boolean toAward(String playerName) {
        switch (type){
            case COMMAND:
                new CraftRewardCommand().parse(sliceReward).toAward(playerName);
                return true;
            case ITEM:
                new CraftRewardItem().parse(sliceReward).toAward(playerName);
                return true;
            case RUNNABLE:
                new CraftRewardRunnable().parse(sliceReward).toAward(playerName);
                return true;
            default:
                return false;
        }
    }
}
