package centralworks.spawners.modules.models.quests;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class QuestRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Expose
    private Long idKey;
    @Expose
    private String id;
    @Expose
    private String interpreter;
    @Expose
    private boolean completed = false;
    @Expose
    private Long completedIn = System.currentTimeMillis();
    @ManyToOne
    @Expose(deserialize = false, serialize = false)
    private QuestData questData;
    @Expose
    private String value;

    public QuestRule(boolean completed) {
        this.completed = completed;
        if (completed) completedIn = System.currentTimeMillis();
    }

    public Double addValueAsDouble(Double value) {
        final Double newValue = getValueAsDouble() + value;
        setValue(newValue.toString());
        return newValue;
    }

    public Integer addValueAsInteger(Integer value) {
        final Integer newValue = getValueAsInteger() + value;
        setValue(newValue.toString());
        return newValue;
    }

    public Double getValueAsDouble() {
        return Double.valueOf(getValueAsString());
    }

    public Boolean getValueAsBoolean() {
        return Boolean.valueOf(getValueAsString().replace(".0", ""));
    }

    public Integer getValueAsInteger() {
        return Integer.valueOf(getValueAsString().replace(".0", ""));
    }

    public String getValueAsString() {
        return value;
    }

}
