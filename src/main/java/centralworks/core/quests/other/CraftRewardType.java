package centralworks.core.quests.other;

import com.google.gson.annotations.SerializedName;

public enum CraftRewardType {
    @SerializedName("COMMAND")
    COMMAND,
    @SerializedName("ITEM")
    ITEM,
    @SerializedName("RUNNABLE")
    RUNNABLE
}
