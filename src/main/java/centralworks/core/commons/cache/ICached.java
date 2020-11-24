package centralworks.core.commons.cache;

import centralworks.core.commons.models.Impulse;
import centralworks.lib.ListCache;

public class ICached extends ListCache<Impulse> {

    private static ICached me;

    public static ICached get() {
        return me == null ? me = new ICached() : me;
    }
}
