package centralworks.core.commons.cache;

import centralworks.lib.Cache;
import centralworks.core.commons.models.Limit;

public class LimitCached extends Cache<Limit> {

    private static LimitCached me;

    public static LimitCached get() {
        return me == null ? me = new LimitCached() : me;
    }
}
