package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.listeners.commons.EntityListeners;
import org.bukkit.Bukkit;

@Module
public class StackMobsModule extends PluginSystem {

    @Override
    public String getId() {
        return "stackmobs";
    }

    @Override
    public void start(Main plugin, Object... objects) {
        Bukkit.getPluginManager().registerEvents(new EntityListeners(), Main.getInstance());
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
