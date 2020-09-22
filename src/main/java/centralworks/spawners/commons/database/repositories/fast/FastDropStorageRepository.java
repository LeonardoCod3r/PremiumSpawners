package centralworks.spawners.commons.database.repositories.fast;

import centralworks.spawners.commons.database.specifications.FastRepository;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;

public class FastDropStorageRepository extends FastRepository<DropStorage, String> {

    private static FastDropStorageRepository repository;

    public static FastDropStorageRepository require() {
        return repository == null ? repository = new FastDropStorageRepository() : null;
    }

    public FastDropStorageRepository() {
        super(DropStorage.class);
    }
}
