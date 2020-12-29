package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.cache.google.Caches;
import centralworks.cache.simple.SICached;
import centralworks.cache.simple.SpawnerRanking;
import centralworks.commands.SpawnersCommand;
import centralworks.listeners.spawners.PlayerListeners;
import centralworks.listeners.spawners.SpawnerListeners;
import centralworks.spawners.models.Spawner;
import centralworks.database.SyncRequests;
import centralworks.hooks.DynmapHook;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class SpawnerModule extends PluginSystem {

    @Override
    public String getId() {
        return "spawners";
    }

    @Override
    public void start(Main plugin) {
        final DynmapHook dynmapHook = plugin.getDynmapHook();
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) dynmapHook.boot();
        for (Spawner spawner : new Spawner().getRepository().findAll()) {
            final SyncRequests<Spawner, Object> query = spawner.query();
            spawner.appear(null);
            query.commit();
        }
        SICached.get().load();
        final SpawnerRanking ranking = SpawnerRanking.get();
        ranking.updateAsync();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> SpawnerRanking.get().updateAsync(), 20L * 60 * 5, 20L * 60 * 7);
        ((CraftServer) plugin.getServer()).getCommandMap().register("spawners", new SpawnersCommand());
        Bukkit.getPluginManager().registerEvents(SpawnerListeners.getListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), plugin);
    }

    @Override
    public void terminate(Main plugin) {
        Caches.getCache(Spawner.class).asMap().values().forEach(Spawner::impulsesForceStop);
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
