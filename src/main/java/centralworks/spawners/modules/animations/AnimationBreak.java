package centralworks.spawners.modules.animations;


import centralworks.spawners.modules.models.spawners.Spawner;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class AnimationBreak implements AnimationService {

    private double radius = 0.7;
    private int ticksToFinalize = 60;
    private boolean cancelled = false;

    public void send(Spawner spawner) {
        final Location location = spawner.getLocation().add(0.5, 2.0, 0.5);
        final ProtocolManager pManager = ProtocolLibrary.getProtocolManager();
        final PacketContainer packet = pManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getModifier().writeDefaults();
        packet.getParticles().write(0, EnumWrappers.Particle.VILLAGER_ANGRY);
        packet.getFloat()
                .write(0, (float) (location.getX()))
                .write(1, (float) location.getY() + 2)
                .write(2, (float) (location.getZ()));
        pManager.broadcastServerPacket(packet);
    }
}
