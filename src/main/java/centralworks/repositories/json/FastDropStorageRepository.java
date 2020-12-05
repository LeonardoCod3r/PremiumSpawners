package centralworks.repositories.json;

import centralworks.models.DropStorage;
import centralworks.database.specifications.FastRepository;

public class FastDropStorageRepository extends FastRepository<DropStorage, String> {

    private static FastDropStorageRepository repository;

    public FastDropStorageRepository() {
        super(DropStorage.class);
    }

    public static FastDropStorageRepository require() {
        return repository == null ? repository = new FastDropStorageRepository() : repository;
    }
}
