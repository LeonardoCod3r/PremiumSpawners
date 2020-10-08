package centralworks.core.commons.cache;

import centralworks.lib.Cache;
import centralworks.core.commons.models.Impulse;

public class ICached extends Cache<Impulse> {

    private static ICached me;

    public static ICached get() {
        return me == null ? me = new ICached() : me;
    }
}
