package centralworks.spawners.commons.database;

import centralworks.spawners.commons.database.specifications.EntityConfiguration;

public abstract class Storable<T extends Storable<T>> implements EntityConfiguration {

    @SuppressWarnings("unchecked")
    public QueriesSync<T> query() {
        return QueriesSync.supply((T) this);
    }

}
