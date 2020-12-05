package centralworks.events;

import centralworks.spawners.models.Spawner;
import centralworks.spawners.models.SpawnerItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpawnerRemoveEvent extends Event implements Cancellable {


    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private Player player;
    @Getter
    private Spawner spawner;
    @Getter
    private SpawnerItem removed;
    @Getter
    private boolean all;
    @Getter
    @Setter
    private boolean cancelled = false;

    public SpawnerRemoveEvent(Player player, Spawner spawner, SpawnerItem removed, boolean all) {
        this.player = player;
        this.spawner = spawner;
        this.removed = removed;
        this.all = all;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }


}
