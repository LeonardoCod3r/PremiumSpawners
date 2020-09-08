package centralworks.spawners.modules.models.addons;

import centralworks.spawners.lib.Cache;

public class ICached extends Cache<Impulse> {

    private static ICached me;

    public static ICached get() {
        return me == null ? me = new ICached() : me;
    }
}
