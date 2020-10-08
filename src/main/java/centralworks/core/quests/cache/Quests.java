package centralworks.core.quests.cache;

import centralworks.lib.Cache;
import centralworks.core.quests.other.CraftQuest;

public class Quests extends Cache<CraftQuest> {

    private static Quests me;

    public static Quests get() {
        return me == null ? me = new Quests() : me;
    }
}
