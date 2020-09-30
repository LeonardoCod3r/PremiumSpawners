package centralworks.spawners.lib.database.repositories.jpa;

import centralworks.spawners.lib.database.specifications.JpaRepository;
import centralworks.spawners.modules.models.spawners.Spawner;

public class JpaSpawnerRepository extends JpaRepository<Spawner, String> {

    private static JpaSpawnerRepository repository;

    public static JpaSpawnerRepository require() {
        return repository == null ? repository = new JpaSpawnerRepository() : repository;
    }

    public JpaSpawnerRepository() {
        super(Spawner.class);
    }
}
