package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.market.MarketLoader;
import lombok.val;

@Module
public class MarketModule extends PluginSystem {

    @Override
    public String getId() {
        return "market";
    }

    @Override
    public void start(Main plugin) {
        val loader = MarketLoader.getInstance();
        loader.load();
        loader.enable();
    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
