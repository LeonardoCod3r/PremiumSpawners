package centralworks.spawners.managers.upgrade;

import centralworks.spawners.managers.upgrade.upgrades.SpawnMobLevel;
import centralworks.spawners.managers.upgrade.upgrades.SpawnMobLevel1;
import centralworks.spawners.managers.upgrade.upgrades.SpawnMobLevel2;

public class UpgradeManager {

    public static SpawnMobLevel SpawnMobLevel;
    public static SpawnMobLevel1 SpawnMobLevel1;
    public static SpawnMobLevel2 SpawnMobLevel2;

    static {
        SpawnMobLevel2 = new SpawnMobLevel2();
        SpawnMobLevel1 = new SpawnMobLevel1();
        SpawnMobLevel = new SpawnMobLevel();

    }

}
