package centralworks.spawners.modules.models.dropsstorage;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.repositories.DropStorageRepository;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.modules.cmds.DSFriendsCommand;
import centralworks.spawners.modules.cmds.SellCommand;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.BonusRegistered;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

public class ApplicationDropStorage {

    public static void boot() {
        LootData.get().load();
        ((CraftServer) Main.get().getServer()).getCommandMap().register("armazem", new SellCommand());
        if (Main.getDropStorage().is("Settings.friends.toggle"))
            ((CraftServer) Main.get().getServer()).getCommandMap().register("armazemfriends", new DSFriendsCommand());
        Main.getDropStorage().section("Bonus").forEach(s -> BonusRegistered.put(s.replace("-", "."), Main.getConfiguration().getInt("Bonus." + s)));
    }

    public static void shutdown() {
        final SyncRequests<DropStorage, String> q = SyncRequests.supply(DropStorageRepository.require());
        q.getDto().findAllFiles().forEach(storage -> {
            storage.getBoostersActive().clear();
            storage.query().commit(true);
        });
        q.getDto().delete();
    }


}
