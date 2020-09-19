package centralworks.spawners.commons.database.repositories;

import centralworks.spawners.commons.database.specifications.CrudRepository;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;

public class DropStorageRepository extends CrudRepository<DropStorage, String> {

    private static DropStorageRepository repository;

    public static DropStorageRepository require() {
        return repository == null ? repository = new DropStorageRepository() : repository;
    }

    public DropStorageRepository() {
        super(DropStorage.class);
    }
}
