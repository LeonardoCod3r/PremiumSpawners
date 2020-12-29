package centralworks.repositories.mysql;

import centralworks.models.ProductStorage;
import centralworks.database.specifications.JpaRepository;

public class JpaDropStorageRepository extends JpaRepository<ProductStorage, String> {

    private static JpaDropStorageRepository repository;

    public JpaDropStorageRepository() {
        super(ProductStorage.class);
    }

    public static JpaDropStorageRepository require() {
        return repository == null ? repository = new JpaDropStorageRepository() : repository;
    }
}
