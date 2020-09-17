package centralworks.spawners.modules.models.quests;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class QuestRule {

    private String id;
    private String interpreter;
    private boolean completed = false;
    private Long completedIn = System.currentTimeMillis();
    private Object value;

    public QuestRule(boolean completed) {
        this.completed = completed;
        if (completed) completedIn = System.currentTimeMillis();
    }

    public Double addValueAsDouble(Double value) {
        final double newValue = getValueAsDouble() + value;
        setValue(newValue);
        return newValue;
    }

    public Integer addValueAsInteger(Integer value) {
        final int newValue = getValueAsInteger() + value;
        setValue(newValue);
        return newValue;
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
