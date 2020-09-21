package centralworks.spawners.modules.animations;


import centralworks.spawners.modules.models.spawners.Spawner;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.Location;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class AnimationBreak extends AnimationService {

    @Expose
    private int ticksToFinalize = 60;

    public void send(Spawner spawner) {
        setLocSerialized(spawner.getLocSerialized());
        setAnimationType(AnimationType.BREAK);
        final Location location = spawner.getLocation().add(0.5, 2.0, 0.5);
        final ProtocolManager pManager = ProtocolLibrary.getProtocolManager();
        final int times = 1;
        for (int i = times; i> 0; i--) {
            final PacketContainer packet = pManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
            packet.getModifier().writeDefaults();
            packet.getParticles().write(0, EnumWrappers.Particle.EXPLOSION_LARGE);
            packet.getFloat()
                    .write(0, (float) (location.getX()))
                    .write(1, (float) location.getY() + 2)
                    .write(2, (float) (location.getZ()));
            pManager.broadcastServerPacket(packet);
        }
    }

}
