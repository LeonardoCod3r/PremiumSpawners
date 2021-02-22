package centralworks.guice;

import centralworks.Main;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data(staticConstructor = "of")
public class PluginModule extends AbstractModule {

    private final Main plugin;

    @Override
    protected void configure() {
        bind(Main.class).toInstance(plugin);
    }

    public Injector createInjector() {
        return Guice.createInjector(this);
    }
}
