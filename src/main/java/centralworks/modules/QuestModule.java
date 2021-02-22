package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.commands.QuestsCommand;
import centralworks.init.QuestLoader;
import centralworks.listeners.quests.PlayerListeners;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class QuestModule extends PluginSystem {

    private boolean questsSystemIsActive;

    @Override
    public String getId() {
        return "quests";
    }

    @Override
    public void start(Main plugin, Object... objects) {
        val questLoader = QuestLoader.get();
        questLoader.setDefaults();
        questLoader.run();
        this.questsSystemIsActive = (boolean) objects[0];
        val map = ((CraftServer) Main.getInstance().getServer()).getCommandMap();
        map.register("quests", new QuestsCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), Main.getInstance());
    }

    @Override
    public void reload(Main plugin) {
        if (questsSystemIsActive) {
            val questLoader = QuestLoader.get();
            questLoader.setLoaded(false);
            questLoader.run();
        }
    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return Main.getInstance().questsSystemIsActive();
    }
}
