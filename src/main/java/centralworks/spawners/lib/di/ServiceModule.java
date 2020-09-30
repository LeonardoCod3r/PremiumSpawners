package centralworks.spawners.lib.di;

import centralworks.spawners.Main;
import centralworks.spawners.lib.database.specifications.JpaRepository;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.dropsstorage.BoosterPlayer;
import centralworks.spawners.modules.models.dropsstorage.DropPlayer;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import centralworks.spawners.modules.models.quests.QuestData;
import centralworks.spawners.modules.models.quests.QuestRule;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerImpulse;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.EntityManager;
import java.util.Properties;

public class ServiceModule extends AbstractModule {

    static {
        Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }

    private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<Session> SESSION_CACHE = new ThreadLocal<>();


    @Override
    protected void configure() {
    }


    @Provides
    @Singleton
    public SessionFactory provideSessionFactory() {
        final Configuration configuration = new Configuration();
        final Properties settings = new Properties();
        defineProps(settings);
        configuration.setProperties(settings);
        configuration.addAnnotatedClass(UserDetails.class);
        configuration.addAnnotatedClass(Spawner.class);
        configuration.addAnnotatedClass(DropStorage.class);
        configuration.addAnnotatedClass(PlayerQuests.class);
        configuration.addAnnotatedClass(QuestData.class);
        configuration.addAnnotatedClass(QuestRule.class);
        configuration.addAnnotatedClass(SpawnerImpulse.class);
        configuration.addAnnotatedClass(BoosterPlayer.class);
        configuration.addAnnotatedClass(DropPlayer.class);
        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    private void defineProps(Properties settings) {
        settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        settings.put(Environment.URL, "jdbc:mysql://localhost:3306/twmobcoins?useSSL=false&useTimezone=true&serverTimezone=UTC");
        settings.put(Environment.USER, "root");
        settings.put(Environment.PASS, "123");
        settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
        settings.put(Environment.SHOW_SQL, "true");
        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        settings.put(Environment.HBM2DDL_AUTO, "update");
        settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        settings.put("hibernate.hikari.minimumIdle", "5");
        settings.put("hibernate.hikari.maximumPoolSize", "50");
        settings.put("hibernate.hikari.idleTimeout", "30000");
    }

    @Provides
    public Session provideSessionManager(SessionFactory sessionFactory) {
        final ThreadLocal<Session> threadLocal = new ThreadLocal<>();
        Session session;
        session = sessionFactory.openSession();
        threadLocal.set(session);
        JpaRepository.setSession(session);
        return session;
    }

    @Provides
    public EntityManager provideEntityManager(SessionFactory sessionFactory) {
        EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
        if (entityManager == null)
            ENTITY_MANAGER_CACHE.set(entityManager = sessionFactory.createEntityManager());
        JpaRepository.setEm(entityManager);
        return entityManager;
    }
}
