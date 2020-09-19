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
        /*final Properties properties = new Properties();
        properties.put("javax.persistence.schema-generation.database.action", "create");
        properties.put(Environment.JPA_PERSISTENCE_PROVIDER, "org.hibernate.jpa.HibernatePersistenceProvider");
        properties.put(Environment.JPA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
        properties.put(Environment.JPA_JDBC_URL, "jdbc:mysql://localhost:3306/twmobcoins?useSSL=false&amp;useTimezone=true&amp;serverTimezone=UTC");
        properties.put(Environment.JPA_JDBC_USER, "root");
        properties.put(Environment.JPA_JDBC_PASSWORD, "123");
        properties.put(Environment.JPA_TRANSACTION_TYPE, "RESOURCE_LOCAL");
        properties.put("hibernate.hikari.minimumIdle", "5");
        properties.put("hibernate.hikari.maximumPoolSize", "10");
        properties.put("hibernate.hikari.idleTimeout", "30000");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");*/
    }

    public static void create() {
        if (getEntityManagerFactory() == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("centralworks.spawners");
            entityManager = entityManagerFactory.createEntityManager();
            Main.get().getLogger().info("Unidade de persistência iniciada.");
        }
    }

    public static void createNew() {
        entityManagerFactory = Persistence.createEntityManagerFactory("centralworks.spawners");
        entityManager = entityManagerFactory.createEntityManager();
        Main.get().getLogger().info("Unidade de persistência criada.");
    }

}
