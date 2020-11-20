package centralworks.core.spawners.models;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
@Entity
public class Statistics implements Cloneable {


    @Id
    @Column(length = 150)
    @Getter(AccessLevel.PRIVATE)
    @Setter
    private String locSerialized;
    @Getter
    @Setter
    @Expose
    @ElementCollection
    @MapKeyColumn(length = 150)
    private Map<String, Integer> map = Maps.newHashMap();
    @Getter(AccessLevel.PRIVATE)
    @Setter
    @OneToOne(mappedBy = "statistics")
    private Spawner spawner;

    public Statistics(HashMap<String, Integer> map) {
        this.map = map;
    }

    public Statistics() {
    }

    public void replace(String statistic, Integer level){
        if (map.containsKey(statistic)) {
            map.replace(statistic, level);
        } else map.put(statistic, level);
    }

    public Integer getLevel(String statistic){
        return map.get(statistic);
    }

    public void upgradeLevel(String statistic){
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
