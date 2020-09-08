package centralworks.spawners.modules.models.quests.cached;

import centralworks.spawners.lib.Cache;
import centralworks.spawners.modules.models.quests.suppliers.CraftQuest;

public class Quests extends Cache<CraftQuest> {

    private static Quests me;

    public static Quests get() {
        return me == null ? me = new Quests() : me;
    }
}
