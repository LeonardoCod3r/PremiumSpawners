package centralworks.repositories.json;

import centralworks.models.ProductStorage;
import centralworks.database.specifications.FastRepository;

public class FastDropStorageRepository extends FastRepository<ProductStorage, String> {

    private static FastDropStorageRepository repository;

    public FastDropStorageRepository() {
        super(ProductStorage.class);
    }

    public static FastDropStorageRepository require() {
        return repository == null ? repository = new FastDropStorageRepository() : repository;
    }
}
