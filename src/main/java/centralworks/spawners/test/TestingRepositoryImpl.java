package centralworks.spawners.test;

import org.hibernate.cfg.Environment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

public class TestingRepositoryImpl {

    private static final Properties properties;

    private final EntityManagerFactory emf;
    private final EntityManager em;

    static {
        //Thread.currentThread().setContextClassLoader(Main.get().getClass().getClassLoader());
        properties = new Properties();
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
        properties.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "centralworks.spawners.test.Testing");
    }

    public TestingRepositoryImpl() {
        emf = Persistence.createEntityManagerFactory("centralworks.spawners");
        em = emf.createEntityManager();
    }

    
    public Testing create(Testing person) {
        em.getTransaction().begin();
        em.persist(person);
        em.getTransaction().commit();
        return person;
    }

    
    public Testing read(Long id) {
        em.getTransaction().begin();
        Testing person = em.find(Testing.class, id);
        em.getTransaction().commit();
        return person;
    }

    
    public Testing update(Testing person) {
        em.getTransaction().begin();
        person = em.merge(person);
        em.getTransaction().commit();
        return person;
    }

    
    public void delete(Testing person) {
        em.getTransaction().begin();
        em.remove(person);
        em.getTransaction().commit();
    }

    public void close() {
        emf.close();
    }
    
}
