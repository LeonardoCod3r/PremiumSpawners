package centralworks.spawners.modules.models.addons;

import com.google.gson.annotations.SerializedName;

public enum ImpulseType {

    @SerializedName("GENERATION")
    GENERATION("geração"),
    @SerializedName("DROPS")
    DROPS("drops");

    private final String name;

    public String getName() {
        return name;
    }

    ImpulseType(String name) {
        this.name = name;
    }
}