package centralworks;

import centralworks.core.dropstorage.cache.LootData;
import centralworks.core.spawners.cache.SICached;
import centralworks.database.specifications.ServiceModule;
import centralworks.hooks.DynmapHook;
import centralworks.init.ImpulseLoader;
import centralworks.init.LimitLoader;
import centralworks.init.QuestLoader;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Settings;
import centralworks.lib.Configuration;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.tr7zw.nbtinjector.NBTInjector;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Animals;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private static Gson gson;
    @Getter
    private Configuration configuration;
    @Getter
    private Configuration spawners;
    @Getter
    private Configuration entities;
    @Getter
    private Configuration dropStorage;
    @Getter
    private Configuration data;
    @Getter
    private Configuration messages;
    @Getter
    private Configuration cacheConfig;
    @Getter
    private Economy vaultAPIEconomy;
    @Getter
    private Injector injector;
    @Getter
    private DynmapHook dynmapHook;
    @Getter
    @Setter
    private Properties properties;

    private void info(String text) {
        getLogger().info(text);
    }

    public void createConfigurations() {
        try {
            for (Field field : Main.class.getDeclaredFields()) {
                if (Modifier.isPrivate(field.getModifiers()) && field.getType().getName().equals(Configuration.class.getName())) {
                    field.setAccessible(true);
                    field.set(this, new Configuration(field.getName().toLowerCase()));
                    field.setAccessible(false);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        info("Aplicando o NBTInjector...");
        NBTInjector.inject();
        info("NBTInjector aplicado!");
        info("Corrigindo entidades...");
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClasses(IronGolem.class, Villager.class, Animals.class, Monster.class).stream().filter(entity -> !entity.hasMetadata("NPC")).forEach(NBTInjector::patchEntity));
        info("Entidades corrigidas!");
    }

    @Override
    public void onEnable() {
        boot();
    }

    public void distribution() {
        info("Distribuindo valores para produção.");
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        dynmapHook = new DynmapHook();
        vaultAPIEconomy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        createConfigurations();
        final Map<String, Double> collect = configuration.getList("Settings.format", true).stream().collect(Collectors.toMap(s -> s.split(",")[0], s -> Double.parseDouble(s.split(",")[1])));
        BalanceFormatter.setSuffix(configuration.getList("Settings.format", true).stream().map(s -> s.replace(s.split(",")[1], "").replace(",", "")).toArray(String[]::new));
        BalanceFormatter.setFormat(Maps.newHashMap(collect));
        info("Distribuição completada com sucesso.");
        this.injector = Guice.createInjector(new ServiceModule());
    }

    public void reload() {
        distribution();
        if (questsSystemIsActive()) {
            final QuestLoader questLoader = QuestLoader.get();
            questLoader.setLoaded(false);
            questLoader.run();
        }
        if (dropStorageSystemIsActive()) {
            LootData.get().load();
        }
        if (limitSystemIsActive()) {
            final LimitLoader limitLoader = LimitLoader.get();
            limitLoader.setLoaded(false);
            limitLoader.run();
        }
        final ImpulseLoader impulseLoader = ImpulseLoader.get();
        impulseLoader.setLoaded(false);
        impulseLoader.run();
        MenusSettings.newInstance();
        SICached.get().load();
    }

    private void boot() {
        info("Inicializando o plug-in...");
        distribution();
        Application.startSystems();
        getData().set("Answered", true);
        getData().save();
        info("Plug-in inicializado e pronto para uso.");
    }

    public boolean questsSystemIsActive() {
        return configuration.is("Settings.quests");
    }

    public boolean dropStorageSystemIsActive() {
        return configuration.is("Settings.dropStorage");
    }

    public boolean limitSystemIsActive() {
        return configuration.is("Settings.limitSystem");
    }

    @Override
    public void onDisable() {
        info("Salvando dados.");
        final long l = System.currentTimeMillis();
        Application.terminateSystems();
        info("Dados salvos com sucesso. Atraso: " + (System.currentTimeMillis() - l) + "ms.");
    }

}
