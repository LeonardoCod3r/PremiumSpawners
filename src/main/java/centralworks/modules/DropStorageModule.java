package centralworks.modules;

import centralworks.Main;
import centralworks.Module;
import centralworks.PluginSystem;
import centralworks.cache.simple.BonusRegistered;
import centralworks.commands.DSFriendsCommand;
import centralworks.commands.SellCommand;
import centralworks.listeners.dropstorage.EntityListeners;
import centralworks.listeners.dropstorage.PlayerListeners;
import centralworks.market.MarketLoader;
import centralworks.market.models.Market;
import centralworks.market.models.Product;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

@Module
public class DropStorageModule extends PluginSystem {

    private boolean dropStorageSystemIsActive;

    @Override
    public String getId() {
        return "dropstorage";
    }

    @Override
    public void start(Main plugin, Object... objects) {
        val map = ((CraftServer) Main.getInstance().getServer()).getCommandMap();
        map.register("armazem", new SellCommand());
        val nav = Main.getInstance().getDropStorage().navigate();
        val loader = MarketLoader.getInstance();
        loader.load();
        loader.enable();
        val market = Market.getInstance();
        market.getProducts().forEach(product -> {
            product.runDelayToResetSoldAmount();
            product.firstValue();
        });
        this.dropStorageSystemIsActive = (boolean) objects[0];
        if (nav.getBoolean("Settings.friends.toggle"))
            map.register("armazemfriends", new DSFriendsCommand());
        nav.section("Bonus").forEach(s -> BonusRegistered.put(s.replace("-", "."), nav.getInt("Bonus." + s)));
        Bukkit.getPluginManager().registerEvents(new EntityListeners(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), plugin);
    }

    @Override
    public void reload(Main plugin) {
        if (dropStorageSystemIsActive) {
            val loader = MarketLoader.getInstance();
            loader.setLoaded(false);
            loader.enable();
            val market = Market.getInstance();
            market.getProducts().forEach(product -> {
                product.runDelayToResetSoldAmount();
                product.firstValue();
            });
        }
    }

    @Override
    public void terminate(Main plugin) {

    }

    @Override
    public boolean canRegister() {
        return Main.getInstance().dropStorageSystemIsActive();
    }
}
