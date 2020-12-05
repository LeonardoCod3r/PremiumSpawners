package centralworks.quests.structure;

import centralworks.lib.ItemSettings;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CraftQuestRule {

    private String id;
    private String type;
    private Object value;
    private String defaultValue;
    private JsonObject additionalInformation;
    private ItemSettings inventoryView;

    public Double getDefaultValueAsDouble() {
        return Double.valueOf(defaultValue);
    }

    public Boolean getDefaultValueAsBoolean() {
        return Boolean.valueOf(getDefaultValueAsString().replace(".0", ""));
    }

    public Integer getDefaultValueAsInteger() {
        return Integer.valueOf(getDefaultValueAsString().replace(".0", ""));
    }

    public String getDefaultValueAsString() {
        return defaultValue;
    }

    public Double getValueAsDouble() {
        return Double.valueOf(getValueAsString());
    }

    public Boolean getValueAsBoolean() {
        return Boolean.valueOf(getValueAsString());
    }

    public Integer getValueAsInteger() {
        return Integer.valueOf(getValueAsString().replace(".0", ""));
    }

    public String getValueAsString() {
        return String.valueOf(value);
    }

}