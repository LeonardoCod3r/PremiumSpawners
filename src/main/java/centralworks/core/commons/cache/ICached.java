package centralworks.core.commons.cache;

import centralworks.core.commons.models.Impulse;
import centralworks.lib.Cache;

public class ICached extends Cache<Impulse> {

    private static ICached me;

    public static ICached get() {
        return me == null ? me = new ICached() : me;
    }
}
