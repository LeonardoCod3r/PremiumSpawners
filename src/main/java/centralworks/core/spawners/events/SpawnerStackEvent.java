package centralworks.core.spawners.events;

import centralworks.core.spawners.models.Spawner;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerStackEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private Player player;
    @Getter
    private Spawner stacked;
    @Getter
    private Spawner beingStacked;
    @Getter
    @Setter
    private boolean cancelled = false;

    public SpawnerStackEvent(Player player, Spawner stacked, Spawner beingStacked) {
        this.player = player;
        this.stacked = stacked;
        this.beingStacked = beingStacked;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}
