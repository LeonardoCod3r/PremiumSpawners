package centralworks.repositories.json;

import centralworks.database.specifications.FastRepository;
import centralworks.core.spawners.models.Spawner;

public class FastSpawnerRepository extends FastRepository<Spawner, String> {

    private static FastSpawnerRepository repository;

    public static FastSpawnerRepository require() {
        return repository == null ? repository = new FastSpawnerRepository() : repository;
    }

    public FastSpawnerRepository() {
        super(Spawner.class);
    }
}
