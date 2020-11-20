package centralworks.core.spawners.events;

import centralworks.core.spawners.models.Spawner;
import centralworks.core.spawners.models.SpawnerItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerBreakEvent extends Event implements Cancellable {


    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private Player player;
    @Getter
    private Spawner spawner;
    @Getter
    private SpawnerItem result;
    @Getter
    @Setter
    private boolean cancelled = false;

    public SpawnerBreakEvent(Player player, Spawner spawner, SpawnerItem spawnerItem) {
        this.player = player;
        this.spawner = spawner;
        this.result = spawnerItem;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

}
