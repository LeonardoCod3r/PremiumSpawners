package centralworks.spawners.modules.animations;

import centralworks.spawners.Main;
import centralworks.spawners.modules.models.spawners.Spawner;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class AnimationPlace extends AnimationService {

    @Expose
    private float red = 255;
    @Expose
    private float green = 0;
    @Expose
    private float blue = 0;
    @Expose
    private int ticksToFinalize = 60;

    public void send(Spawner spawner) {
        setLocSerialized(spawner.getLocSerialized());
        setAnimationType(AnimationType.PLACE);
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
                    final Spawner s = spawner.query().queue().getTarget();
                    s.cancelAnimation();
                    s.query().commit();
                    cancel();
                    return;
                }
                if (ticksToFinalize == duration) {
                    new BukkitRunnable() {
                        int times = 4;
                        @Override
                        public void run() {
                            if (times == 0) {
                                cancel();
                                return;
                            }
                            final PacketContainer packet = pManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
                            packet.getModifier().writeDefaults();
                            packet.getParticles().write(0, EnumWrappers.Particle.CLOUD);
                            packet.getFloat()
                                    .write(0, (float) (location.getX()))
                                    .write(1, (float) location.getY() + 1)
                                    .write(2, (float) (location.getZ()));
                            pManager.broadcastServerPacket(packet);
                            times--;
                        }
                    }.runTaskTimer(Main.get(), 0, 5);
                    final Spawner s = spawner.query().queue().getTarget();
                    s.cancelAnimation();
                    s.query().commit();
                    cancel();
                    return;
                }
                final double x = radius * Math.sin(time);
                final double z = radius * Math.cos(time);
                final PacketContainer packet = pManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
                packet.getModifier().writeDefaults();
                packet.getParticles().write(0, EnumWrappers.Particle.VILLAGER_HAPPY);
                packet.getFloat()
                        .write(0, (float) (location.getX() + x))
                        .write(1, (float) location.getY() + 1)
                        .write(2, (float) (location.getZ() + z))
                        .write(3, red)
                        .write(4, green)
                        .write(5, blue);
                pManager.broadcastServerPacket(packet);
                time += 0.1;
                duration += 1;
            }
        }.runTaskTimer(Main.get(), 20, 1);
    }
}
