package centralworks.spawners.commons.database.repositories.jpa;

import centralworks.spawners.commons.database.specifications.JpaRepository;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;

public class JpaDropStorageRepository extends JpaRepository<DropStorage, String> {

    private static JpaDropStorageRepository repository;

    public static JpaDropStorageRepository require() {
        return repository == null ? repository = new JpaDropStorageRepository() : repository;
    }

    public JpaDropStorageRepository() {
        super(DropStorage.class);
    }
}
