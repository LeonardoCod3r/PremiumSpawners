package centralworks.repositories.mysql;

import centralworks.database.specifications.JpaRepository;
import centralworks.core.spawners.models.Spawner;

public class JpaSpawnerRepository extends JpaRepository<Spawner, String> {

    private static JpaSpawnerRepository repository;

    public static JpaSpawnerRepository require() {
        return repository == null ? repository = new JpaSpawnerRepository() : repository;
    }

    public JpaSpawnerRepository() {
        super(Spawner.class);
    }
}
