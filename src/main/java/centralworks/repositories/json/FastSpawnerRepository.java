package centralworks.repositories.json;

import centralworks.core.spawners.models.Spawner;
import centralworks.database.specifications.FastRepository;

public class FastSpawnerRepository extends FastRepository<Spawner, String> {

    private static FastSpawnerRepository repository;

    public FastSpawnerRepository() {
        super(Spawner.class);
    }

    public static FastSpawnerRepository require() {
        return repository == null ? repository = new FastSpawnerRepository() : repository;
    }
}
