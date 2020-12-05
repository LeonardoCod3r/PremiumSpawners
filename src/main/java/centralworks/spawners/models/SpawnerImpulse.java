package centralworks.spawners.models;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.enums.ImpulseType;
import com.google.common.cache.LoadingCache;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.Bukkit;

import javax.persistence.*;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class SpawnerImpulse {

    @Id
    @GeneratedValue
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Long id;
    @Enumerated
    @Getter
    @Setter
    @Expose
    private ImpulseType impulseType;
    @Getter
    @Setter
    @Expose
    private Double value = 1.0;
    @Getter
    @Setter
    @Expose
    private Long startedAt = System.currentTimeMillis();
    @Getter
    @Setter
    @Expose
    private Long finish;
    @Getter
    @Setter
    @Expose
    private Long delay;
    @Getter
    @Setter
    @Expose
    private boolean valid;
    @Getter
    @Setter
    @Expose
    private Integer idTask = 0;
    @Getter(AccessLevel.PRIVATE)
    @Setter
    @ManyToOne
    private Spawner spawner;

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
     *
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
    public void in(Spawner spawner) {
        setValid(true);
        spawner.addImpulse(this);
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        this.idTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
            setValid(false);
            spawner1.removeImpulse(this);
        }), delay).getTaskId();
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
    public void in(Spawner spawner, Runnable callback) {
        setValid(true);
        spawner.addImpulse(this);
        run(spawner, callback);
    }

    public void run(Spawner spawner, Runnable callback) {
        setSpawner(spawner);
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        this.idTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
            setValid(false);
            spawner1.removeImpulse(this, callback);
        }), delay).getTaskId();
    }
}
