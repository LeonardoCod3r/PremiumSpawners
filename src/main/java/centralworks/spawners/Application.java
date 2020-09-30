package centralworks.spawners;

import centralworks.spawners.lib.database.SyncRequests;
import centralworks.spawners.modules.cmds.DSFriendsCommand;
import centralworks.spawners.modules.cmds.QuestsCommand;
import centralworks.spawners.modules.cmds.SellCommand;
import centralworks.spawners.modules.cmds.SpawnersCommand;
import centralworks.spawners.modules.hook.DynmapHook;
import centralworks.spawners.modules.hook.PlaceHolderHook;
import centralworks.spawners.modules.listeners.EntityListeners;
import centralworks.spawners.modules.listeners.PlayerListeners;
import centralworks.spawners.modules.listeners.SpawnerListeners;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.addons.ImpulseLoader;
import centralworks.spawners.modules.models.addons.LimitLoader;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.BonusRegistered;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import centralworks.spawners.modules.models.quests.suppliers.QuestLoader;
import centralworks.spawners.modules.models.ranking.SpawnerRanking;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.cached.SICached;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.List;

public class Application {

    private static final ThreadLocal<List<PluginSystem>> CACHE = new ThreadLocal<>();

    public static void registerPluginSystem(PluginSystem... pluginSystem) {
        final List<PluginSystem> list = Lists.newArrayList(CACHE.get());
        list.addAll(Lists.newArrayList(pluginSystem));
        CACHE.set(list);
    }

    public static void startSystems() {
        CACHE.get().forEach(pluginSystem -> pluginSystem.start(Main.getInstance()));
    }

    public static void terminateSystems() {
        CACHE.get().forEach(pluginSystem -> pluginSystem.terminate(Main.getInstance()));
    }

    public static void startSystem(String id) {
        getPluginSystem(id).start(Main.getInstance());
    }

    public static void stopSystem(String id) {
        getPluginSystem(id).terminate(Main.getInstance());
    }

    private static PluginSystem getPluginSystem(String id) {
        return CACHE.get().stream().filter(pluginSystem -> pluginSystem.getId().equals(id)).findFirst().orElse(null);
    }

    static {
        CACHE.set(Lists.newArrayList(
                new PluginSystem() {
                    @Override
                    public String getId() {
                        return "commons";
                    }

                    @Override
                    public void start(Main plugin) {
                        MenusSettings.get();
                        final ImpulseLoader impulseLoader = ImpulseLoader.get();
                        impulseLoader.setDefaults();
                        impulseLoader.run();
                        if (Main.getInstance().limitSystemIsActive()) {
                            final LimitLoader limitLoader = LimitLoader.get();
                            limitLoader.setDefaults();
                            limitLoader.run();
                            new PlaceHolderHook().register();
                        }
                    }

                    @Override
                    public void terminate(Main plugin) {

                    }
                },
                new PluginSystem() {
                    @Override
                    public String getId() {
                        return "spawners";
                    }

                    @Override
                    public void start(Main plugin) {
                        final DynmapHook dynmapHook = plugin.getDynmapHook();
                        final Injector injector = plugin.getInjector();
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
                        ((CraftServer) plugin.getServer()).getCommandMap().register("spawners", injector.getInstance(SpawnersCommand.class));
                        Bukkit.getPluginManager().registerEvents(injector.getInstance(SpawnerListeners.class), plugin);
                        Bukkit.getPluginManager().registerEvents(injector.getInstance(EntityListeners.class), plugin);
                        Bukkit.getPluginManager().registerEvents(injector.getInstance(PlayerListeners.class), plugin);
                    }

                    @Override
                    public void terminate(Main plugin) {
                        new Spawner().getRepository().findAll().forEach(Spawner::impulsesForceStop);
                    }
                },
                new PluginSystem() {
                    @Override
                    public String getId() {
                        return "quests";
                    }

                    @Override
                    public void start(Main plugin) {
                        final QuestLoader questLoader = QuestLoader.get();
                        questLoader.setDefaults();
                        questLoader.run();
                        ((CraftServer) Main.getInstance().getServer()).getCommandMap().register("quests", new QuestsCommand());
                    }

                    @Override
                    public void terminate(Main plugin) {

                    }
                },
                new PluginSystem() {
                    @Override
                    public String getId() {
                        return "dropstorage";
                    }

                    @Override
                    public void start(Main plugin) {
                        final Injector injector = plugin.getInjector();
                        LootData.get().load();
                        ((CraftServer) Main.getInstance().getServer()).getCommandMap().register("armazem", injector.getInstance(SellCommand.class));
                        if (Main.getInstance().getDropStorage().is("Settings.friends.toggle"))
                            ((CraftServer) Main.getInstance().getServer()).getCommandMap().register("armazemfriends", injector.getInstance(DSFriendsCommand.class));
                        Main.getInstance().getDropStorage().section("Bonus").forEach(s -> BonusRegistered.put(s.replace("-", "."), Main.getInstance().getDropStorage().getInt("Bonus." + s)));
                    }

                    @Override
                    public void terminate(Main plugin) {

                    }
                }
        ));
    }

}
