package centralworks.lib;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

@UtilityClass
public class NPCManager {

    public static NPC spawn(Location location, String npcName) {
        val npcRegistry = CitizensAPI.getNPCRegistry();
        val npc = npcRegistry.createNPC(EntityType.PLAYER, npcName);
        npc.setFlyable(true);
        npc.setProtected(true);
        npc.data().setPersistent("player-skin-name", npcName);
        npc.spawn(location);
        npc.setName(npcName);
        npc.getTrait(LookClose.class).toggle();
        return npc;
    }


}
