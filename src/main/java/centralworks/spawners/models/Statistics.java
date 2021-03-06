package centralworks.spawners.models;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Entity
@Data
@NoArgsConstructor
public class Statistics implements Cloneable {

    @Id
    @Column(length = 150)
    @Getter(AccessLevel.PRIVATE)
    private String locSerialized;
    @Expose
    @ElementCollection
    @MapKeyColumn(length = 150)
    private Map<String, Integer> map = Maps.newHashMap();
    @Getter(AccessLevel.PRIVATE)
    @OneToOne(mappedBy = "statistics")
    private Spawner spawner;

    public Statistics(Spawner spawner, HashMap<String, Integer> map) {
        this.spawner = spawner;
        this.locSerialized = spawner.getLocSerialized();
        this.map = map;
    }

    public Statistics(Spawner spawner) {
        this.spawner = spawner;
        this.locSerialized = spawner.getLocSerialized();
    }

    public void replace(String statistic, Integer level) {
        if (map.containsKey(statistic)) {
            map.replace(statistic, level);
        } else map.put(statistic, level);
    }

    public Integer getLevel(String statistic) {
        return map.get(statistic);
    }

    public void upgradeLevel(String statistic) {
        replace(statistic, getLevel(statistic) + 1);
    }

    public void remove(String statistic) {
        map.remove(statistic);
    }

    @Override
    public Statistics clone() throws CloneNotSupportedException {
        return (Statistics) super.clone();
    }
}
