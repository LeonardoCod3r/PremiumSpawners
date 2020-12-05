package centralworks.models.enums;

import com.google.gson.annotations.SerializedName;

public enum ImpulseType {

    @SerializedName("GENERATION")
    GENERATION("geração"),
    @SerializedName("DROPS")
    DROPS("drops");

    private final String name;

    ImpulseType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
