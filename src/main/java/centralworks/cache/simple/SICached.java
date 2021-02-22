package centralworks.cache.simple;

import centralworks.Main;
import centralworks.lib.ListCache;
import centralworks.spawners.models.SpawnerItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;

public class SICached extends ListCache<SICached.LoadedSpawnerItem> {

    private static SICached me;

    public static SICached get() {
        return me == null ? me = new SICached() : me;
    }

    public void load() {
        clear();
        Main.getInstance().getSpawners().navigate().section("List").stream().map(EntityType::valueOf).forEach(entityType -> add(new LoadedSpawnerItem(entityType)));
    }

    @Data
    @RequiredArgsConstructor
    public static class LoadedSpawnerItem {

        private EntityType entityType;
        private SpawnerItem spawnerItem;

        public LoadedSpawnerItem(EntityType entityType) {
            this.entityType = entityType;
            this.spawnerItem = new SpawnerItem(entityType);
        }
    }
}
