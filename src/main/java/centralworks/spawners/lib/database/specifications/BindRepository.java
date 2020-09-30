package centralworks.spawners.lib.database.specifications;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.Storable;

public class BindRepository<O extends Storable<O>, T> {

    private Class<O> clazz;
    private FastRepository<O, T> fastRepository;
    private JpaRepository<O, T> jpaRepository;

    public BindRepository(Class<O> clazz, JpaRepository<O, T> jpaRepository, FastRepository<O, T> fastRepository) {
        this.clazz = clazz;
        this.fastRepository = fastRepository;
        this.jpaRepository = jpaRepository;
    }

    public Repository<O, T> getRelativeRepository() {
        final String db = Main.getInstance().getData().get("Database", false);
        if (db.equalsIgnoreCase("MYSQL")) return jpaRepository;
        else return fastRepository;
    }
}
