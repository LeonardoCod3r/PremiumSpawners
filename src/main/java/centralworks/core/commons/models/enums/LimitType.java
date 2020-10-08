package centralworks.core.commons.models.enums;

import com.google.gson.annotations.SerializedName;

public enum LimitType {

    @SerializedName("BUY")
    BUY("compra"),
    @SerializedName("SELL")
    SELL("venda");

    private final String name;

    LimitType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
