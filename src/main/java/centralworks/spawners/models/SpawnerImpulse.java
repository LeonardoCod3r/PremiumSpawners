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
@NoArgsConstructor
@Entity
@Data
public class SpawnerImpulse {

    @Id
    @GeneratedValue
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Long id;
    @Enumerated
    @Expose
    private ImpulseType impulseType;
    @Expose
    private Double value = 1.0;
    @Expose
    private Long startedAt = System.currentTimeMillis();
    @Expose
    private Long finish;
    @Expose
    private Long delay;
    @Expose
    private boolean valid = false;
    @Expose
    private Integer idTask = 0;
    @Getter(AccessLevel.PRIVATE)
    @ManyToOne
    private Spawner spawner;

    /**
     * @param impulseType type of impulse
     * @param delay       delay in seconds
     * @param value       multiplier
     */
    public SpawnerImpulse(ImpulseType impulseType, Integer delay, Double value) {
        this.delay = (long) delay;
        this.impulseType = impulseType;
        this.value = value;
    }

    private void configureDelay() {
        this.startedAt = System.currentTimeMillis();
        this.finish = startedAt + (delay * 1000);
    }

    public void stop() {
        if (isValid()) {
            setValid(false);
            this.delay = (finish - System.currentTimeMillis()) / 1000;
            Bukkit.getScheduler().cancelTask(this.idTask);
            this.idTask = null;
        }
    }

    /**
     * @param spawner  to apply
     * @param callback which will be accepted if it ends
     */
    public void in(Spawner spawner, Runnable callback) {
        spawner.addImpulse(this);
        run(spawner, callback);
    }

    public void run(Spawner spawner, Runnable callback) {
        setValid(true);
        setSpawner(spawner);
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        configureDelay();
        this.idTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Optional.ofNullable(cache.getIfPresent(spawner.getLocSerialized())).ifPresent(spawner1 -> {
            setValid(false);
            if (callback != null) spawner1.removeImpulse(this, callback);
            else spawner1.removeImpulse(this);
        }), delay * 20L).getTaskId();
    }
}
