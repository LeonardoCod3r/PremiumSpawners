package centralworks.spawners;

import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.modules.cmds.BoosterCommand;
import centralworks.spawners.modules.cmds.LimitCommand;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.ApplicationCommons;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.dropsstorage.ApplicationDropStorage;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.DropC;
import centralworks.spawners.modules.models.spawners.cached.SICached;
import centralworks.spawners.modules.models.addons.LimitLoader;
import centralworks.spawners.modules.models.quests.ApplicationQuest;
import centralworks.spawners.modules.models.quests.suppliers.QuestLoader;
import centralworks.spawners.modules.models.spawners.ApplicationSpawner;
import centralworks.spawners.modules.models.addons.ImpulseLoader;
import com.google.gson.Gson;
import de.tr7zw.nbtinjector.NBTInjector;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private static Main me;
    private static Gson gson;
    private static Configuration configuration;
    private static Configuration spawners;
    private static Configuration entities;
    private static Configuration limits;
    private static Configuration data;
    private static Configuration messages;
    private static Economy economy;

    public static Gson getGson() {
        return gson == null ? gson = new Gson() : gson;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Configuration getSpawners() {
        return spawners;
    }

    public static Configuration getEntities() {
        return entities;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static Configuration getLimits() {
        return limits;
    }

    public static Configuration getMessages() {
        return messages;
    }

    public static Configuration getData() {
        return data;
    }

    public static Main get() {
        return me;
    }

    private static Logger LOGGER;


    @Override
    public void onLoad() {
        LOGGER = getLogger();
        LOGGER.info("Aplicando o NBTInjector...");
        NBTInjector.inject();
        LOGGER.info("NBTInjector aplicado!");
        LOGGER.info("Corrigindo entidades...");
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClasses(Animals.class, Monster.class).forEach(NBTInjector::patchEntity));
        LOGGER.info("Entidades corrigidas!");
    }

    @Override
    public void onEnable() {
        boot();
    }

    public void distribution() {
        LOGGER.info("Distribuindo valores para produção.");
        me = this;
        messages = new Configuration("messages");
        configuration = new Configuration("configuration");
        spawners = new Configuration("spawners");
        entities = new Configuration("entities");
        limits = new Configuration("limits");
        data = new Configuration("data");
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        FormatBalance.suffix = configuration.getList("Settings.format", true).stream().map(s -> s.replace(s.split(",")[1], "").replace(",", "")).toArray(String[]::new);
        configuration.getList("Settings.format", true).forEach(s -> FormatBalance.format.put(s.split(",")[0], Double.valueOf(s.split(",")[1])));
        LOGGER.info("Distribuição completada com sucesso.");
    }

    public void reload(){
        distribution();
        final QuestLoader questLoader = QuestLoader.get();
        questLoader.setLoaded(false);
        questLoader.run();
        final ImpulseLoader impulseLoader = ImpulseLoader.get();
        impulseLoader.setLoaded(false);
        impulseLoader.run();
        final LimitLoader limitLoader = LimitLoader.get();
        limitLoader.setLoaded(false);
        limitLoader.run();
        MenusSettings.newInstance();
        DropC.get().load();
        SICached.get().load();
    }

    public void registerOtherCommands() {
        final SimpleCommandMap map = ((CraftServer) getServer()).getCommandMap();
        map.register("booster", new BoosterCommand());
        map.register("limit", new LimitCommand());
    }

    private void boot() {
        LOGGER.info("Inicializando o plug-in...");
        distribution();
        ApplicationCommons.boot();
        ApplicationDropStorage.boot();
        ApplicationSpawner.boot();
        ApplicationQuest.boot();
        registerOtherCommands();
        Main.getData().set("Answered", true);
        Main.getData().save();
        LOGGER.info("Plug-in inicializado e pronto para uso.");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Salvando dados.");
        final long l = System.currentTimeMillis();
        ApplicationSpawner.shutdown();
        ApplicationQuest.shutdown();
        ApplicationDropStorage.shutdown();
        ApplicationCommons.shutdown();
        LOGGER.info("Dados salvos com sucesso. Atraso: " + (System.currentTimeMillis() - l) + "ms.");
    }
}
