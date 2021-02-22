package centralworks.cache.simple;

import centralworks.lib.ListCache;
import centralworks.quests.structure.CraftQuest;

public class Quests extends ListCache<CraftQuest> {

    private static Quests me;

    public static Quests get() {
        return me == null ? me = new Quests() : me;
    }
}
