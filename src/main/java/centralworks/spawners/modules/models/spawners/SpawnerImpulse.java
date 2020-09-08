package centralworks.spawners.modules.models.spawners;

import centralworks.spawners.Main;
import centralworks.spawners.modules.models.addons.ImpulseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SpawnerImpulse {

    private ImpulseType impulseType;
    private Double value = 1.0;
    private Long startedAt = System.currentTimeMillis();
    private Long finish;
    private Long delay;
    private boolean valid;
    private Integer idTask = 0;

    /**
     * @param impulseType type of impulse
     * @param delay       delay in seconds
     * @param value       multiplier
     */
    public SpawnerImpulse(ImpulseType impulseType, Integer delay, Double value) {
        this.finish = startedAt + (delay * 1000);
        this.delay = delay * 20L;
        this.impulseType = impulseType;
        this.value = value;
    }

    /**
     * no use this
     * @return object fixed
     */
    public SpawnerImpulse fix() {
        this.startedAt = System.currentTimeMillis();
        this.finish = startedAt + (delay * 1000);
        this.delay = delay * 20L;
        return this;
    }

    /**
     * @param spawner to apply
     */
    public void go(Spawner spawner) {
        setValid(true);
        spawner.addImpulse(this);
        this.idTask = Bukkit.getScheduler().runTaskLater(Main.get(), () -> spawner.query().queue((spawner1, query) -> {
            setValid(false);
            spawner1.removeImpulse(this);
            query.commit();
        }), delay).getTaskId();
        spawner.query().commit();
    }

    public void stop() {
        if (isValid()) {
            setValid(false);
            this.delay = (finish - System.currentTimeMillis()) / 1000;
            Bukkit.getScheduler().cancelTask(this.idTask);
            this.idTask = 0;
        }
    }

    /**
     * @param spawner  to apply
     * @param callback which will be accepted if it ends
     */
    public void go(Spawner spawner, Runnable callback) {
        setValid(true);
        spawner.addImpulse(this);
        run(spawner, callback);
        spawner.query().commit();
    }

    public void run(Spawner spawner, Runnable callback){
        this.idTask = Bukkit.getScheduler().runTaskLater(Main.get(), () -> spawner.query().queue((spawner1, q) -> {
            setValid(false);
            spawner1.removeImpulse(this, callback);
            q.commit();
        }), delay).getTaskId();
    }
}
