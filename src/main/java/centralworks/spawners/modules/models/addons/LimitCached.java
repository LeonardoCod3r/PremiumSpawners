package centralworks.spawners.modules.models.addons;

import centralworks.spawners.lib.Cache;

public class LimitCached extends Cache<Limit> {

    private static LimitCached me;

    public static LimitCached get() {
        return me == null ? me = new LimitCached() : me;
    }
}
