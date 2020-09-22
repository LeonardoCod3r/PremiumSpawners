package centralworks.spawners;

import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.commons.database.repositories.SpawnerRepository;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.modules.cmds.BoosterCommand;
import centralworks.spawners.modules.cmds.LimitCommand;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.ApplicationCommons;
import centralworks.spawners.modules.models.addons.ImpulseLoader;
import centralworks.spawners.modules.models.addons.LimitLoader;
import centralworks.spawners.modules.models.dropsstorage.ApplicationDropStorage;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import centralworks.spawners.modules.models.quests.ApplicationQuest;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import centralworks.spawners.modules.models.quests.suppliers.QuestLoader;
import centralworks.spawners.modules.models.spawners.ApplicationSpawner;
import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.cached.SICached;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tr7zw.nbtinjector.NBTInjector;
import lombok.Getter;
import lombok.val;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private static Main me;
    private static Gson gson;
    @Getter
    private static Configuration configuration;
    @Getter
    private static Configuration spawners;
    @Getter
    private static Configuration entities;
    @Getter
    private static Configuration dropStorage;
    @Getter
    private static Configuration data;
    @Getter
    private static Configuration messages;
    @Getter
    private static Economy economy;
    private static Logger LOGGER;

    public static Gson getGson() {
        return gson == null ? gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }).create() : gson;
    }

    public static Main get() {
        return me;
    }

    @Override
    public void onLoad() {
        LOGGER = getLogger();
        LOGGER.info("Aplicando o NBTInjector...");
        NBTInjector.inject();
        LOGGER.info("NBTInjector aplicado!");
        LOGGER.info("Corrigindo entidades...");
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClasses(IronGolem.class, Villager.class, Animals.class, Monster.class).stream().filter(entity -> !entity.hasMetadata("NPC")).forEach(NBTInjector::patchEntity));
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
        dropStorage = new Configuration("dropstorage");
        data = new Configuration("data");
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        FormatBalance.suffix = configuration.getList("Settings.format", true).stream().map(s -> s.replace(s.split(",")[1], "").replace(",", "")).toArray(String[]::new);
        configuration.getList("Settings.format", true).forEach(s -> FormatBalance.format.put(s.split(",")[0], Double.valueOf(s.split(",")[1])));
        LOGGER.info("Distribuição completada com sucesso.");
    }

    public void reload() {
        distribution();
        if (questsSystemIsActive()) {
            final QuestLoader questLoader = QuestLoader.get();
            questLoader.setLoaded(false);
            questLoader.run();
        }
        if (dropStorageSystemIsActive()) LootData.get().load();
        final ImpulseLoader impulseLoader = ImpulseLoader.get();
        impulseLoader.setLoaded(false);
        impulseLoader.run();
        if (limitSystemIsActive()) {
            final LimitLoader limitLoader = LimitLoader.get();
            limitLoader.setLoaded(false);
            limitLoader.run();
        }
        MenusSettings.newInstance();
        SICached.get().load();
    }

    public void registerOtherCommands() {
        final SimpleCommandMap map = ((CraftServer) getServer()).getCommandMap();
        map.register("booster", new BoosterCommand());
        if (limitSystemIsActive()) map.register("limit", new LimitCommand());
    }

    private void boot() {
        LOGGER.info("Inicializando o plug-in...");
        distribution();
        ApplicationCommons.boot();
        ApplicationSpawner.boot();
        if (dropStorageSystemIsActive()) ApplicationDropStorage.boot();
        if (questsSystemIsActive()) ApplicationQuest.boot();
        registerOtherCommands();
        Main.getData().set("Answered", true);
        Main.getData().save();
        LOGGER.info("Plug-in inicializado e pronto para uso.");
    }

    public boolean questsSystemIsActive() {
        return getConfiguration().is("Settings.quests");
    }

    public boolean dropStorageSystemIsActive() {
        return getConfiguration().is("Settings.dropStorage");
    }

    public boolean limitSystemIsActive() {
        return getConfiguration().is("Settings.limitSystem");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Salvando dados.");
        final long l = System.currentTimeMillis();
        ApplicationSpawner.shutdown();
        if (questsSystemIsActive()) ApplicationQuest.shutdown();
        if (dropStorageSystemIsActive()) ApplicationDropStorage.shutdown();
        ApplicationCommons.shutdown();
        LOGGER.info("Dados salvos com sucesso. Atraso: " + (System.currentTimeMillis() - l) + "ms.");
    }
}
