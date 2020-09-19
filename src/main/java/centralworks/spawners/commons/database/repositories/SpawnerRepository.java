package centralworks.spawners.commons.database.repositories;

import centralworks.spawners.commons.database.specifications.CrudRepository;
import centralworks.spawners.modules.models.spawners.Spawner;

public class SpawnerRepository extends CrudRepository<Spawner, String> {

    private static SpawnerRepository repository;

    public static SpawnerRepository require() {
        return repository == null ? repository = new SpawnerRepository() : repository;
    }

    public SpawnerRepository() {
        super(Spawner.class);
    }
}
