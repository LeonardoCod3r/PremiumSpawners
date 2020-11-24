package centralworks.core.quests.cache;

import centralworks.core.quests.other.CraftQuest;
import centralworks.lib.ListCache;

public class Quests extends ListCache<CraftQuest> {

    private static Quests me;

    public static Quests get() {
        return me == null ? me = new Quests() : me;
    }
}
