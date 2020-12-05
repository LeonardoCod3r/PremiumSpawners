package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.commands.QuestsCommand;
import centralworks.listeners.quests.PlayerListeners;
import centralworks.init.QuestLoader;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class QuestModule extends PluginSystem {

    @Override
    public String getId() {
        return "quests";
    }

    @Override
    public void start(Main plugin) {
        final QuestLoader questLoader = QuestLoader.get();
        questLoader.setDefaults();
        questLoader.run();
        final SimpleCommandMap map = ((CraftServer) Main.getInstance().getServer()).getCommandMap();
        map.register("quests", new QuestsCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), Main.getInstance());
    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return Main.getInstance().questsSystemIsActive();
    }
}
