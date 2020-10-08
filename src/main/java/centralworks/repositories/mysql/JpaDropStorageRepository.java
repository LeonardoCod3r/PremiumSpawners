package centralworks.repositories.mysql;

import centralworks.database.specifications.JpaRepository;
import centralworks.core.dropstorage.models.DropStorage;

public class JpaDropStorageRepository extends JpaRepository<DropStorage, String> {

    private static JpaDropStorageRepository repository;

    public static JpaDropStorageRepository require() {
        return repository == null ? repository = new JpaDropStorageRepository() : repository;
    }

    public JpaDropStorageRepository() {
        super(DropStorage.class);
    }
}
