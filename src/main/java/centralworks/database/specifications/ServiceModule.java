package centralworks.database.specifications;

import centralworks.Main;
import centralworks.core.commons.models.User;
import centralworks.core.dropstorage.models.BoosterPlayer;
import centralworks.core.dropstorage.models.DropPlayer;
import centralworks.core.dropstorage.models.DropStorage;
import centralworks.core.quests.models.PlayerQuests;
import centralworks.core.quests.models.QuestData;
import centralworks.core.quests.models.QuestRule;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
import centralworks.core.spawners.models.Statistics;
import centralworks.lib.Settings;
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
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Spawner.class);
        configuration.addAnnotatedClass(DropStorage.class);
        configuration.addAnnotatedClass(PlayerQuests.class);
        configuration.addAnnotatedClass(QuestData.class);
        configuration.addAnnotatedClass(QuestRule.class);
        configuration.addAnnotatedClass(SpawnerImpulse.class);
        configuration.addAnnotatedClass(BoosterPlayer.class);
        configuration.addAnnotatedClass(DropPlayer.class);
        configuration.addAnnotatedClass(Statistics.class);
        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    private void defineProps(Properties settings) {
        final Settings.Navigate data = Main.getInstance().getData().navigate();
        settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        settings.put(Environment.URL, "jdbc:mysql://" + data.getString("MySQL.Host") + ":" + data.getString("MySQL.Port") + "/" + data.getString("MySQL.Database") + "?useSSL=false&useTimezone=true&serverTimezone=UTC");
        settings.put(Environment.USER, data.getString("MySQL.User"));
        settings.put(Environment.PASS, data.getString("MySQL.Password"));
        settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
        settings.put(Environment.SHOW_SQL, data.getString("Advanced.showSql"));
        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        settings.put(Environment.HBM2DDL_AUTO, "update");
        settings.put(Environment.CONNECTION_PROVIDER, "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        settings.put("hibernate.hikari.minimumIdle", data.getString("Advanced.minimumIdle"));
        settings.put("hibernate.hikari.maximumPoolSize", data.getString("Advanced.maximumPoolSize"));
        settings.put("hibernate.hikari.idleTimeout", data.getString("Advanced.idleTimeout"));
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
