package centralworks.core.spawners.events;

import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerImpulse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@EqualsAndHashCode(callSuper = true)
public class BoosterActiveEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final SpawnerImpulse spawnerImpulse;
    @Getter
    private final Spawner spawner;
    @Getter
    @Setter
    private boolean cancelled = false;

    public BoosterActiveEvent(Player player, SpawnerImpulse spawnerImpulse, Spawner spawner) {
        this.player = player;
        this.spawnerImpulse = spawnerImpulse;
        this.spawner = spawner;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
