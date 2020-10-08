package centralworks.database;

import centralworks.database.specifications.EntityConfiguration;

public abstract class Storable<O extends Storable<O>> implements EntityConfiguration {

    @SuppressWarnings("unchecked")
    public <T> SyncRequests<O, T> query() {
        return SyncRequests.supply(this.getRepository(), (O) this);
    }

}
