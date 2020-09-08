package centralworks.spawners.modules.events;

import centralworks.spawners.modules.models.spawners.Spawner;
import centralworks.spawners.modules.models.spawners.SpawnerImpulse;
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
    @Setter
    private boolean cancelled = false;
    @Getter
    private final Player player;
    @Getter
    private final SpawnerImpulse spawnerImpulse;
    @Getter
    private final Spawner spawner;

    public BoosterActiveEvent(Player player, SpawnerImpulse spawnerImpulse, Spawner spawner) {
        this.player = player;
        this.spawnerImpulse = spawnerImpulse;
        this.spawner = spawner;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
