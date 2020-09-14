package centralworks.spawners.modules.models.spawners;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.QueriesSync;
import centralworks.spawners.modules.cmds.SpawnersCommand;
import centralworks.spawners.modules.hook.DynmapHook;
import centralworks.spawners.modules.hook.PlaceHolderHook;
import centralworks.spawners.modules.listeners.EntityListeners;
import centralworks.spawners.modules.listeners.PlayerListeners;
import centralworks.spawners.modules.listeners.SpawnerListeners;
import centralworks.spawners.modules.models.ranking.SpawnerRanking;
import centralworks.spawners.modules.models.spawners.cached.SICached;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationSpawner {

    @Getter
    private static DynmapHook dynmapHook;

    public static void boot() {
        dynmapHook = new DynmapHook();
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) dynmapHook.boot();
        final QueriesSync<Spawner> queriesSync = QueriesSync.supply(Spawner.class);
        queriesSync.getDao().createTable();
        for (Spawner spawner : queriesSync.getDao().loadAll()) {
            spawner.query().queue((spawner1, q) -> {
                spawner1.getLocation().getChunk().load();
                spawner1.pullHologram();
                spawner1.impulsesForceRun();
                q.commit();
            });
        }
        SICached.get().load();
        SpawnerRanking.get().updateAsync();
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.get(), () -> SpawnerRanking.get().updateAsync(), 20L * 60 * 5, 20L * 60 * 8);
        ((CraftServer) Main.get().getServer()).getCommandMap().register("spawners", new SpawnersCommand());
        Bukkit.getPluginManager().registerEvents(new SpawnerListeners(), Main.get());
        Bukkit.getPluginManager().registerEvents(new EntityListeners(), Main.get());
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), Main.get());
        new PlaceHolderHook().register();
    }

    public static void shutdown() {
        final QueriesSync<Spawner> q = QueriesSync.supply(Spawner.class);
        q.getDto().findAllFiles().forEach(spawner -> {
            spawner.getImpulsesOfGeneration().forEach(SpawnerImpulse::stop);
            spawner.query().commit(true);
        });
        q.getDto().delete();
        HologramsAPI.getHolograms(Main.get()).forEach(Hologram::delete);
    }
}
