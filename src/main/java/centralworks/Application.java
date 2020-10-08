package centralworks;

import centralworks.cache.Caches;
import centralworks.core.commons.cmds.BoosterCommand;
import centralworks.core.commons.cmds.LimitCommand;
import centralworks.core.commons.listeners.PlayerListeners;
import centralworks.hooks.PlaceHolderHook;
import centralworks.layouts.settings.MenusSettings;
import centralworks.init.ImpulseLoader;
import centralworks.init.LimitLoader;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.util.LinkedList;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class Application {

    private static final ThreadLocal<LinkedList<PluginSystem>> CACHE = new ThreadLocal<>();

    @Getter
    @Setter
    private static Function<LinkedList<PluginSystem>, LinkedList<PluginSystem>> orderToStart;
    @Getter
    @Setter
    private static Function<LinkedList<PluginSystem>, LinkedList<PluginSystem>> orderToTerminate;

    public static void registerPluginSystem(PluginSystem... pluginSystem) {
        final LinkedList<PluginSystem> list = Lists.newLinkedList(CACHE.get());
        list.addAll(Lists.newArrayList(pluginSystem));
        CACHE.set(list);
    }

    public static void startSystems() {
        LinkedList<PluginSystem> systems = CACHE.get();
        if (orderToStart != null) systems = orderToStart.apply(systems);
        systems.stream().filter(PluginSystem::canRegister).forEach(pluginSystem -> pluginSystem.start(Main.getInstance()));
    }

    public static void terminateSystems() {
        LinkedList<PluginSystem> systems = CACHE.get();
        if (orderToTerminate != null) systems = orderToTerminate.apply(systems);
        systems.stream().filter(PluginSystem::canRegister).forEach(pluginSystem -> pluginSystem.terminate(Main.getInstance()));
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
        CACHE.set(Lists.newLinkedList());
        registerPluginSystem(new PluginSystem() {
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
                Bukkit.getPluginManager().registerEvents(new PlayerListeners(), Main.getInstance());
                final SimpleCommandMap map = ((CraftServer) Main.getInstance().getServer()).getCommandMap();
                map.register("booster", new BoosterCommand());
                if (plugin.limitSystemIsActive()) map.register("limit", new LimitCommand());
            }

            @Override
            public void terminate(Main plugin) {
                Caches.getCaches().values().forEach(abstractCache -> abstractCache.getCache().invalidateAll());
            }

            @Override
            public boolean canRegister() {
                return true;
            }
        });
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith("centralworks.modules.")) {
                    final Class<?> clazz = info.load();
                    if (clazz.isAnnotationPresent(Module.class)) {
                        final PluginSystem ps = (PluginSystem) clazz.newInstance();
                        registerPluginSystem(ps);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
