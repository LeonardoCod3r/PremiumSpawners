package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.core.dropstorage.cmds.DSFriendsCommand;
import centralworks.core.dropstorage.cmds.SellCommand;
import centralworks.core.dropstorage.cache.BonusRegistered;
import centralworks.core.dropstorage.cache.LootData;
import centralworks.core.dropstorage.listeners.PlayerListeners;
import centralworks.core.dropstorage.listeners.EntityListeners;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class DropStorageModule extends PluginSystem {
    @Override
    public String getId() {
        return "dropstorage";
    }

    @Override
    public void start(Main plugin) {
        LootData.get().load();
        final SimpleCommandMap map = ((CraftServer) Main.getInstance().getServer()).getCommandMap();
        map.register("armazem", new SellCommand());
        if (Main.getInstance().getDropStorage().is("Settings.friends.toggle"))
            map.register("armazemfriends", new DSFriendsCommand());
        Main.getInstance().getDropStorage().section("Bonus").forEach(s -> BonusRegistered.put(s.replace("-", "."), Main.getInstance().getDropStorage().getInt("Bonus." + s)));
        Bukkit.getPluginManager().registerEvents(new EntityListeners(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), plugin);
    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return Main.getInstance().dropStorageSystemIsActive();
    }
}
