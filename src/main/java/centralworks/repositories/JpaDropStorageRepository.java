package centralworks.repositories;

import centralworks.Main;
import centralworks.database.JpaRepository;
import centralworks.models.ProductStorage;

public class JpaDropStorageRepository extends JpaRepository<ProductStorage, String> {

    private static JpaDropStorageRepository repository;

    public JpaDropStorageRepository() {
        super(ProductStorage.class);
    }

    public static JpaDropStorageRepository require() {
        return repository == null ? repository = Main.getInstance().getInjector().getInstance(JpaDropStorageRepository.class) : repository;
    }
}
