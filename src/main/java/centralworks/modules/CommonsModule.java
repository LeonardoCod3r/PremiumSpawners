package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.cache.google.Caches;
import centralworks.commands.BoosterCommand;
import centralworks.commands.LimitCommand;
import centralworks.hooks.PlaceHolderHook;
import centralworks.init.ImpulseLoader;
import centralworks.init.LimitLoader;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.inventory.InventoryController;
import centralworks.listeners.commons.PlayerListeners;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class CommonsModule extends PluginSystem {

    private boolean limitSystemIsActive = false;

    @Override
    public String getId() {
        return "commons";
    }

    @Override
    public void start(Main plugin, Object... objects) {
        MenusSettings.get();
        val impulseLoader = ImpulseLoader.get();
        impulseLoader.setDefaults();
        impulseLoader.run();
        this.limitSystemIsActive = (boolean) objects[0];
        if (limitSystemIsActive) {
            val limitLoader = LimitLoader.get();
            limitLoader.setDefaults();
            limitLoader.run();
            new PlaceHolderHook().register();
        }
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), plugin);
        Bukkit.getPluginManager().registerEvents(InventoryController.getInstance(), plugin);
        val map = ((CraftServer) plugin.getServer()).getCommandMap();
        map.register("booster", new BoosterCommand());
        if (plugin.limitSystemIsActive()) map.register("limit", new LimitCommand());
    }

    @Override
    public void reload(Main plugin) {
        if (limitSystemIsActive) {
            val limitLoader = LimitLoader.get();
            limitLoader.setLoaded(false);
            limitLoader.run();
        }
        val impulseLoader = ImpulseLoader.get();
        impulseLoader.setLoaded(false);
        impulseLoader.run();
        MenusSettings.newInstance();
    }

    @Override
    public void terminate(Main plugin) {
        Caches.getCaches().values().forEach(abstractCache -> abstractCache.getCache().invalidateAll());
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
