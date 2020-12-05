package centralworks.events;

import centralworks.spawners.models.Spawner;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerPlaceEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private Player player;
    @Getter
    private Spawner spawner;
    @Getter
    @Setter
    private boolean cancelled = false;

    public SpawnerPlaceEvent(Player player, Spawner spawner) {
        this.player = player;
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
