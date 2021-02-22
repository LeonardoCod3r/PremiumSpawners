package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;

@Module
public class MarketModule extends PluginSystem {

    @Override
    public String getId() {
        return "market";
    }

    @Override
    public void start(Main plugin, Object... objects) {

    }

    @Override
    public void reload(Main plugin) {

    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
