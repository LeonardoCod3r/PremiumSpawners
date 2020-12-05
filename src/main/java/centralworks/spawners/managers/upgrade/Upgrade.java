package centralworks.spawners.managers.upgrade;

import centralworks.spawners.models.Spawner;
import centralworks.spawners.models.Statistics;
import centralworks.hooks.EconomyContext;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public abstract class Upgrade {

    public abstract int getLevel();

    public abstract String getId();

    public abstract boolean isLast();

    public abstract boolean isDefault();

    public abstract Upgrade getNext();

    public abstract Object getValue();

    public abstract String getName();

    public abstract List<String> getDescription();

    public abstract String getSuccessMessage();

    public abstract HashMap<EconomyContext.Context, Double> getPrice();

    public abstract boolean upgrade(Player p, Spawner spawner);

    public boolean upgradeToNext(Player p, Spawner spawner) {
        if (isLast()) return false;
        else if (getNext().upgrade(p, spawner)) {
            getNext().upgrade(spawner, p);
            return true;
        } else return false;
    }

    public void upgrade(Spawner spawner, Player p) {
        final EconomyContext.Context context = getPrice().keySet().stream().findFirst().get();
        final EconomyContext.Economy eco = context.getEconomy();
        final Double price = getPrice().get(context);
        eco.removeMoney(p.getName(), price);
        final Statistics statistics = spawner.getStatistics();
        statistics.upgradeLevel(getId());
        if (getSuccessMessage() != null && !getSuccessMessage().isEmpty())
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getSuccessMessage()));
    }

}
