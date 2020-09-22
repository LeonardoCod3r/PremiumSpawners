package centralworks.spawners.commons.database.specifications;

import centralworks.spawners.Main;
import lombok.Getter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Storage {

    @Getter
    private static EntityManagerFactory entityManagerFactory;
    @Getter
    private static EntityManager entityManager;

    static {
        Thread.currentThread().setContextClassLoader(Main.get().getClass().getClassLoader());
    }

    public static void create() {
        if (getEntityManagerFactory() == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("centralworks.spawners");
            entityManager = entityManagerFactory.createEntityManager();
//            Main.get().getLogger().info("Unidade de persistência iniciada.");
        }
    }

    public static void createNew() {
        entityManagerFactory = Persistence.createEntityManagerFactory("centralworks.spawners");
        entityManager = entityManagerFactory.createEntityManager();
        Main.get().getLogger().info("Unidade de persistência criada.");
    }

}
