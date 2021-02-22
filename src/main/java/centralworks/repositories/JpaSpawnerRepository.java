package centralworks.repositories;

import centralworks.Main;
import centralworks.database.JpaRepository;
import centralworks.spawners.models.Spawner;

public class JpaSpawnerRepository extends JpaRepository<Spawner, String> {

    private static JpaSpawnerRepository repository;

    public JpaSpawnerRepository() {
        super(Spawner.class);
    }

    public static JpaSpawnerRepository require() {
        return repository == null ? repository = Main.getInstance().getInjector().getInstance(JpaSpawnerRepository.class) : repository;
    }
}
