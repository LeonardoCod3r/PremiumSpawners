package centralworks.lib;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBarMessage {

    public ActionBarMessage(Player p, String message) {
        final PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

}
