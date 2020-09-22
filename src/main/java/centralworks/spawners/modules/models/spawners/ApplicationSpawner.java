package centralworks.spawners.modules.models.spawners;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.repositories.jpa.JpaSpawnerRepository;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.modules.cmds.SpawnersCommand;
import centralworks.spawners.modules.hook.DynmapHook;
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
        final SyncRequests<Spawner, String> queriesSync = SyncRequests.supply(JpaSpawnerRepository.require());
        for (Spawner spawner : queriesSync.getRepository().findAll()) {
            spawner.query().queue((spawner1, q) -> {
                spawner1.getLocation().getChunk().load();
                spawner1.appear(null);
                q.commit();
            });
        }
        SICached.get().load();
        final SpawnerRanking ranking = SpawnerRanking.get();
        ranking.updateAsync();
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.get(), () -> SpawnerRanking.get().updateAsync(), 20L * 60 * 5, 20L * 60 * 7);
        ((CraftServer) Main.get().getServer()).getCommandMap().register("spawners", new SpawnersCommand());
        Bukkit.getPluginManager().registerEvents(new SpawnerListeners(), Main.get());
        Bukkit.getPluginManager().registerEvents(new EntityListeners(), Main.get());
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), Main.get());
    }

    public static void shutdown() {
        final SyncRequests<Spawner, String> q = SyncRequests.supply(JpaSpawnerRepository.require());
        /*q.getDto().findAllFiles().forEach(spawner -> {
            spawner.getImpulsesOfGeneration().forEach(SpawnerImpulse::stop);
            spawner.query().commit();
        });
        q.getDto().delete();*/
        HologramsAPI.getHolograms(Main.get()).forEach(Hologram::delete);
    }
}
