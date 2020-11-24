package centralworks.core.spawners.managers.upgrade.upgrades;

import centralworks.core.spawners.managers.upgrade.Upgrade;
import centralworks.core.spawners.managers.upgrade.UpgradeManager;
import centralworks.core.spawners.models.Spawner;
import centralworks.hooks.EconomyContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class SpawnMobLevel extends Upgrade {

    @Override
    public int getLevel() {
        return 0;
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
        return true;
    }

    @Override
    public Upgrade getNext() {
        return UpgradeManager.SpawnMobLevel1;
    }

    @Override
    public Object getValue() {
        return 40;
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
        return "";
    }

    @Override
    public HashMap<EconomyContext.Context, Double> getPrice() {
        return Maps.newHashMap();
    }

    @Override
    public boolean upgrade(Player p, Spawner spawner) {
        return false;
    }

}
