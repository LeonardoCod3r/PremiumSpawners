package centralworks.database.specifications;

import centralworks.Main;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.dropstorage.models.BoosterPlayer;
import centralworks.core.dropstorage.models.DropPlayer;
import centralworks.core.dropstorage.models.DropStorage;
import centralworks.core.quests.models.PlayerQuests;
import centralworks.core.quests.models.QuestData;
import centralworks.core.quests.models.QuestRule;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
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

    private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<Session> SESSION_CACHE = new ThreadLocal<>();

    static {
        Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }

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
        final centralworks.lib.Configuration data = Main.getInstance().getData();
        settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        settings.put(Environment.URL, "jdbc:mysql://" + data.get("MySQL.Host", false) + ":" + data.get("MySQL.Port", false) + "/" + data.get("MySQL.Database", false) + "?useSSL=false&useTimezone=true&serverTimezone=UTC");
        settings.put(Environment.USER, data.get("MySQL.User", false));
        settings.put(Environment.PASS, data.get("MySQL.Password", false));
        settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
        settings.put(Environment.SHOW_SQL, data.get("Advanced.showSql", false));
        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        settings.put(Environment.HBM2DDL_AUTO, "update");
        settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        settings.put("hibernate.hikari.minimumIdle", data.get("Advanced.minimumIdle", false));
        settings.put("hibernate.hikari.maximumPoolSize", data.get("Advanced.maximumPoolSize", false));
        settings.put("hibernate.hikari.idleTimeout", data.get("Advanced.idleTimeout", false));
    }

    @Provides
    public Session provideSessionManager(SessionFactory sessionFactory) {
        Session session = SESSION_CACHE.get();
        if (session == null) SESSION_CACHE.set(session = sessionFactory.openSession());
        JpaRepository.setSession(session);
        return session;
    }

    @Provides
    public EntityManager provideEntityManager(SessionFactory sessionFactory) {
        EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
        if (entityManager == null) ENTITY_MANAGER_CACHE.set(entityManager = sessionFactory.createEntityManager());
        JpaRepository.setEm(entityManager);
        return entityManager;
    }
}
