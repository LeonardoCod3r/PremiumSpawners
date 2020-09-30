package centralworks.spawners.lib.di;

import centralworks.spawners.Main;
import com.google.inject.AbstractModule;
import lombok.SneakyThrows;

public class MainModule extends AbstractModule {

    @SneakyThrows
    @Override
    protected void configure() {
        bind(Main.class).toInstance(Main.getInstance());
    }
}
