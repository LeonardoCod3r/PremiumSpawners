package centralworks.spawners.modules.models.spawners.utils;

import centralworks.spawners.modules.models.spawners.Spawner;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.function.Predicate;

@Data
@RequiredArgsConstructor
public class FilteringFunctions {

    private List<Spawner> spawnerList;

    public FilteringFunctions(List<Spawner> spawnerList) {
        this.spawnerList = spawnerList;
    }

    public boolean exists(EntityType entityType){
        return spawnerList.stream().anyMatch(spawner -> spawner.getEntityType().equals(entityType));
    }

    public boolean exists(Location location){
        return spawnerList.stream().anyMatch(spawner -> spawner.getLocation().equals(location));
    }

    public boolean exists(Predicate<Spawner> existsFunction){
        return spawnerList.stream().anyMatch(existsFunction);
    }

    public Spawner get(EntityType entityType){
        return spawnerList.stream().filter(spawner -> spawner.getEntityType().equals(entityType)).findFirst().get();
    }

    public Spawner get(Location location){
        return spawnerList.stream().filter(spawner -> spawner.getLocation().equals(location)).findFirst().get();
    }

    public Spawner get(Predicate<Spawner> getFunction){
        return spawnerList.stream().filter(getFunction).findFirst().get();
    }

}
