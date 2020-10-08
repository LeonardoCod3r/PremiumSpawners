package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.database.SyncRequests;
import centralworks.core.spawners.cmds.SpawnersCommand;
import centralworks.hooks.DynmapHook;
import centralworks.core.spawners.listeners.PlayerListeners;
import centralworks.core.spawners.listeners.SpawnerListeners;
import centralworks.core.spawners.cache.SpawnerRanking;
import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.cache.SICached;
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
        Bukkit.getPluginManager().registerEvents(new SpawnerListeners(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), plugin);
    }

    @Override
    public void terminate(Main plugin) {
        new Spawner().getRepository().findAll().forEach(Spawner::impulsesForceStop);
    }

    @Override
    public boolean canRegister() {
        return true;
    }
}
