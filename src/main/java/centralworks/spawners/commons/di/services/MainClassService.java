package centralworks.spawners.commons.di.services;

import centralworks.spawners.Main;
import com.google.inject.AbstractModule;

public class MainClassService extends AbstractModule {
    @Override
    protected void configure() {
        bind(Main.class).toInstance(Main.get());
    }
}
