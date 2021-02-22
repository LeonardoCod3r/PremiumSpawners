package centralworks.spawners.managers.upgrade.upgrades;

import centralworks.hooks.EconomyContext;
import centralworks.lib.enums.PluginSystemType;
import centralworks.spawners.managers.upgrade.Upgrade;
import centralworks.spawners.models.Spawner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class SpawnMobLevel1 extends Upgrade {

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public String getId() {
        return "spawnmob";
    }

    @Override
    public boolean isLast() {
        return false;
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
        return 35;
    }

    @Override
    public String getName() {
        return "§6Delay para gerar monstro";
    }

    @Override
    public List<String> getDescription() {
        return Lists.newArrayList("§eQuanto maior o nível, menos tempo para gerar o monstro.", "§fTempo atual: §7" + getValue());
    }

    @Override
    public String getSuccessMessage() {
        return "§eParabéns! Você evoluiu \"" + getName() + "\" para o nível " + getLevel() + ".";
    }

    @Override
    public HashMap<EconomyContext.Context, Double> getPrice() {
        final HashMap<EconomyContext.Context, Double> map = Maps.newHashMap();
        map.put(EconomyContext.getContext(PluginSystemType.SPAWNERS_STATISTICS), 1000.0);
        return map;
    }

    @Override
    public boolean upgrade(Player p, Spawner spawner) {
        final EconomyContext.Context context = getPrice().keySet().stream().findFirst().get();
        final EconomyContext.Economy eco = context.getEconomy();
        final Double price = getPrice().get(context);
        if (eco.hasMoney(p.getName(), price)) {
            eco.removeMoney(p.getName(), price);
            //todo evoluir o nível do gerador
            return true;
        }
        return false;
    }

}
