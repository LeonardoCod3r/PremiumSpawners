package centralworks.guice;

import centralworks.Main;
import centralworks.lib.Settings;
import centralworks.models.BoosterPlayer;
import centralworks.models.ProductStorage;
import centralworks.models.User;
import centralworks.models.UserProduct;
import centralworks.quests.models.PlayerQuests;
import centralworks.quests.models.QuestData;
import centralworks.quests.models.QuestRule;
import centralworks.spawners.models.Spawner;
import centralworks.spawners.models.SpawnerImpulse;
import centralworks.spawners.models.Statistics;
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
        configuration.addAnnotatedClass(ProductStorage.class);
        configuration.addAnnotatedClass(PlayerQuests.class);
        configuration.addAnnotatedClass(QuestData.class);
        configuration.addAnnotatedClass(QuestRule.class);
        configuration.addAnnotatedClass(SpawnerImpulse.class);
        configuration.addAnnotatedClass(BoosterPlayer.class);
        configuration.addAnnotatedClass(UserProduct.class);
        configuration.addAnnotatedClass(Statistics.class);
        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    private void defineProps(Properties settings) {
        final Settings.Navigate data = Main.getInstance().getData().navigate();
        settings.put(Environment.DRIVER, data.getString("Database.driver"));
        settings.put(Environment.URL, data.getString("Database.url"));
        settings.put(Environment.USER, data.getString("Database.user"));
        settings.put(Environment.PASS, data.getString("Database.password"));
        settings.put(Environment.DIALECT, data.getString("Database.dialect"));
        settings.put(Environment.SHOW_SQL, data.getString("Database.showSql"));
        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, data.getString("Database.currentSessionContextClass"));
        settings.put(Environment.HBM2DDL_AUTO, data.getString("Database.hbm2ddlAuto"));
        settings.put(Environment.CONNECTION_PROVIDER, data.getString("Database.connectionProvider"));
        settings.put("hibernate.hikari.minimumIdle", data.getString("Database.minimumIdle"));
        settings.put("hibernate.hikari.maximumPoolSize", data.getString("Database.maximumPoolSize"));
        settings.put("hibernate.hikari.idleTimeout", data.getString("Database.idleTimeout"));
    }

    @Provides
    public Session provideSessionManager(SessionFactory sessionFactory) {
        Session session = SESSION_CACHE.get();
        if (session == null) SESSION_CACHE.set(session = sessionFactory.openSession());
        return session;
    }

    @Provides
    public EntityManager provideEntityManager(SessionFactory sessionFactory) {
        EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
        if (entityManager == null) ENTITY_MANAGER_CACHE.set(entityManager = sessionFactory.createEntityManager());
        return entityManager;
    }
}
