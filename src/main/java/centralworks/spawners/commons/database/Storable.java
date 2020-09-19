package centralworks.spawners.commons.database;

import centralworks.spawners.commons.database.specifications.EntityConfiguration;

public abstract class Storable<O extends Storable<O>> implements EntityConfiguration {

    @SuppressWarnings("unchecked")
    public <T> SyncRequests<O, T> query() {
        return SyncRequests.supply(this.getRepository(), (O) this);
    }

}
