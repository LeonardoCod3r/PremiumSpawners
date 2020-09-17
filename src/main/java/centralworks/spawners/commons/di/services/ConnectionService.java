package centralworks.spawners.commons.di.services;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionService extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public synchronized Connection provideConnection() {
        try {
            final Configuration configuration = Main.getData();
            final String password = configuration.get("MySQL.Password", false);
            final String user = configuration.get("MySQL.User", false);
            final String host = configuration.get("MySQL.Host", false);
            final String port = configuration.get("MySQL.Port", false);
            final String database = configuration.get("MySQL.Database", false);
            final String type = "jdbc:mysql://";
            final String url = type + host + ":" + port + "/" + database;
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
