package centralworks.core.commons.cache;

import centralworks.core.commons.models.Limit;
import centralworks.lib.ListCache;

public class LimitCached extends ListCache<Limit> {

    private static LimitCached me;

    public static LimitCached get() {
        return me == null ? me = new LimitCached() : me;
    }
}
