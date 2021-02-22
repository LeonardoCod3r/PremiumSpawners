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
import centralworks.repositories.JpaSpawnerRepository;
import centralworks.spawners.models.Spawner;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class SpawnerModule extends PluginSystem {

    @Override
    public String getId() {
        return "spawners";
    }

    @Override
    public void start(Main plugin, Object... objects) {
        val dynmapHook = plugin.getDynmapHook();
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) dynmapHook.boot();
        for (Spawner spawner : JpaSpawnerRepository.require().findAll()) {
            spawner.appear(null);
            JpaSpawnerRepository.require().commit(spawner);
        }
        SICached.get().load();
        val ranking = SpawnerRanking.get();
        ranking.updateAsync();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> SpawnerRanking.get().updateAsync(), 20L * 60 * 5, 20L * 60 * 7);
        ((CraftServer) plugin.getServer()).getCommandMap().register("spawners", new SpawnersCommand());
        Bukkit.getPluginManager().registerEvents(new SpawnerListeners(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), plugin);
    }

    @Override
    public void reload(Main plugin) {
        SICached.get().load();
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
