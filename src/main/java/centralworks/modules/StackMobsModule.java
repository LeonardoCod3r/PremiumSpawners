package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.core.stackmobs.listeners.EntityListeners;
import org.bukkit.Bukkit;

@Module
public class StackMobsModule extends PluginSystem {

    @Override
    public String getId() {
        return "stackmobs";
    }

    @Override
    public void start(Main plugin) {
        Bukkit.getPluginManager().registerEvents(new EntityListeners(), Main.getInstance());
    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
