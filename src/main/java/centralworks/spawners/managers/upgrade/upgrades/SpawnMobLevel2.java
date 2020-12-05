package centralworks.spawners.managers.upgrade.upgrades;

import centralworks.spawners.managers.upgrade.Upgrade;
import centralworks.spawners.models.Spawner;
import centralworks.hooks.EconomyContext;
import centralworks.lib.enums.PluginSystemType;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class SpawnMobLevel2 extends SpawnMobLevel {

    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public boolean isLast() {
        return true;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public Upgrade getNext() {
        return null;
    }

    @Override
    public Object getValue() {
        return 30;
    }

    @Override
    public HashMap<EconomyContext.Context, Double> getPrice() {
        final HashMap<EconomyContext.Context, Double> map = Maps.newHashMap();
        map.put(EconomyContext.getContext(PluginSystemType.QUESTS), 1000.0);
        return map;
    }

    @Override
    public boolean upgrade(Player p, Spawner spawner) {
        final EconomyContext.Context context = getPrice().keySet().stream().findFirst().get();
        final EconomyContext.Economy eco = context.getEconomy();
        final Double price = getPrice().get(context);
        if (eco.hasMoney(p.getName(), price)) {
            //todo evoluir o nível do gerador
            return true;
        }
        return false;
    }


}
