package centralworks.cache.simple;

import centralworks.lib.ListCache;
import centralworks.models.Impulse;

public class ICached extends ListCache<Impulse> {

    private static ICached me;

    public static ICached get() {
        return me == null ? me = new ICached() : me;
    }
}
