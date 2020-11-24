package centralworks.database.specifications;

import centralworks.Main;
import centralworks.database.Storable;

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
        final String db = Main.getInstance().getData().navigate().getString("Database");
        if (db.equalsIgnoreCase("MYSQL")) return jpaRepository;
        else return fastRepository;
    }
}
