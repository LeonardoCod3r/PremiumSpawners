package centralworks.cache.simple;

import centralworks.lib.ListCache;
import centralworks.models.Limit;

public class LimitCached extends ListCache<Limit> {

    private static LimitCached me;

    public static LimitCached get() {
        return me == null ? me = new LimitCached() : me;
    }
}
