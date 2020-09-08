package centralworks.spawners.modules.models.spawners.cached;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Cache;
import centralworks.spawners.modules.models.spawners.SpawnerItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;

public class SICached extends Cache<SICached.LoadedSpawnerItem> {

    private static SICached me;

    public static SICached get() {
        return me == null ? me = new SICached() : me;
    }

    public void load() {
        clear();
        Main.getSpawners().section("List").stream().map(EntityType::valueOf).forEach(entityType -> add(new LoadedSpawnerItem(entityType)));
    }


    @Data
    @RequiredArgsConstructor
    public static class LoadedSpawnerItem {

        private EntityType entityType;
        private SpawnerItem spawnerItem;

        public LoadedSpawnerItem(EntityType entityType) {
            this.entityType = entityType;
            this.spawnerItem = new SpawnerItem().parse(entityType);
        }
    }
}
