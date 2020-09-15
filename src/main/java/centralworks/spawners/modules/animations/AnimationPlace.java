package centralworks.spawners.modules.animations;

import centralworks.spawners.Main;
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
import org.bukkit.scheduler.BukkitRunnable;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class AnimationPlace implements AnimationService {

    private Spawner spawner;
    private float red = 255;
    private float green = 0;
    private float blue = 0;
    private double radius = 0.7;
    private int ticksToFinalize = 60;
    private boolean cancelled = false;

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelled) end();
    }

    private void end() {
        spawner.setAnimationService(null);
        spawner.query().commit();
    }

    public void send() {
        final Location location = spawner.getLocation().add(0.5, 0.0, 0.5);
        spawner.setAnimationService(this);
        spawner.query().commit();
        final ProtocolManager pManager = ProtocolLibrary.getProtocolManager();
        new BukkitRunnable() {
            int duration = 0;
            double time = 0;
            @Override
            public void run() {
                if (isCancelled()) {
                    end();
                    cancel();
                    return;
                }
                if (ticksToFinalize == duration) {
                    final PacketContainer packet = pManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
                    packet.getModifier().writeDefaults();
                    packet.getParticles().write(0, EnumWrappers.Particle.EXPLOSION_NORMAL);
                    packet.getFloat()
                            .write(0, (float) (location.getX()))
                            .write(1, (float) location.getY() + 2)
                            .write(2, (float) (location.getZ()));
                    pManager.broadcastServerPacket(packet);
                    end();
                    cancel();
                    return;
                }
                final double x = radius*Math.sin(time);
                final double z = radius*Math.cos(time);
                final PacketContainer packet = pManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
                packet.getModifier().writeDefaults();
                packet.getParticles().write(0, EnumWrappers.Particle.DRAGON_BREATH);
                packet.getFloat()
                        .write(0, (float) (location.getX() + x))
                        .write(1, (float) location.getY() + 1)
                        .write(2, (float) (location.getZ() + z))
                        .write(3, red)
                        .write(4, green)
                        .write(5, blue);
                pManager.broadcastServerPacket(packet);
                time+=0.1;
                duration+=1;
            }
        }.runTaskTimer(Main.get(), 20, 1);
    }
}
