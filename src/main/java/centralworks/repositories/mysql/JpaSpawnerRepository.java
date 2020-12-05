package centralworks.repositories.mysql;

import centralworks.spawners.models.Spawner;
import centralworks.database.specifications.JpaRepository;

public class JpaSpawnerRepository extends JpaRepository<Spawner, String> {

    private static JpaSpawnerRepository repository;

    public JpaSpawnerRepository() {
        super(Spawner.class);
    }

    public static JpaSpawnerRepository require() {
        return repository == null ? repository = new JpaSpawnerRepository() : repository;
    }
}
