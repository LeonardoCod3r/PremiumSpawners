package centralworks.spawners.modules.models.quests.suppliers;

import com.google.gson.annotations.SerializedName;

public enum CraftRewardType {
    @SerializedName("COMMAND")
    COMMAND,
    @SerializedName("ITEM")
    ITEM,
    @SerializedName("RUNNABLE")
    RUNNABLE
}
