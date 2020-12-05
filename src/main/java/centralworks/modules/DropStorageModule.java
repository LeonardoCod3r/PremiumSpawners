package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.cache.simple.BonusRegistered;
import centralworks.cache.simple.LootData;
import centralworks.commands.DSFriendsCommand;
import centralworks.commands.SellCommand;
import centralworks.listeners.dropstorage.EntityListeners;
import centralworks.listeners.dropstorage.PlayerListeners;
import centralworks.lib.Settings;
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
        final Settings.Navigate nav = Main.getInstance().getDropStorage().navigate();
        if (nav.getBoolean("Settings.friends.toggle"))
            map.register("armazemfriends", new DSFriendsCommand());
        nav.section("Bonus").forEach(s -> BonusRegistered.put(s.replace("-", "."), nav.getInt("Bonus." + s)));
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
