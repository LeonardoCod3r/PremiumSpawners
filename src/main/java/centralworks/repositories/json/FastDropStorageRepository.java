package centralworks.repositories.json;

import centralworks.database.specifications.FastRepository;
import centralworks.core.dropstorage.models.DropStorage;

public class FastDropStorageRepository extends FastRepository<DropStorage, String> {

    private static FastDropStorageRepository repository;

    public static FastDropStorageRepository require() {
        return repository == null ? repository = new FastDropStorageRepository() : repository;
    }

    public FastDropStorageRepository() {
        super(DropStorage.class);
    }
}
