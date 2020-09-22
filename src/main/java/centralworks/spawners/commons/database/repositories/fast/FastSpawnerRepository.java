package centralworks.spawners.commons.database.repositories.fast;

import centralworks.spawners.commons.database.specifications.FastRepository;
import centralworks.spawners.modules.models.spawners.Spawner;

public class FastSpawnerRepository extends FastRepository<Spawner, String> {

    private static FastSpawnerRepository repository;

    public static FastSpawnerRepository require() {
        return repository == null ? repository = new FastSpawnerRepository() : repository;
    }

    public FastSpawnerRepository() {
        super(Spawner.class);
    }
}
