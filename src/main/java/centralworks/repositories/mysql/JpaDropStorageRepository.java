package centralworks.repositories.mysql;

import centralworks.models.DropStorage;
import centralworks.database.specifications.JpaRepository;

public class JpaDropStorageRepository extends JpaRepository<DropStorage, String> {

    private static JpaDropStorageRepository repository;

    public JpaDropStorageRepository() {
        super(DropStorage.class);
    }

    public static JpaDropStorageRepository require() {
        return repository == null ? repository = new JpaDropStorageRepository() : repository;
    }
}
