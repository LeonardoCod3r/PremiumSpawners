package centralworks.spawners.modules.models.dropsstorage.supliers;

import centralworks.spawners.lib.Item;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@Data
@RequiredArgsConstructor
@Builder
public class Drop {

    private final String keyDrop;
    private final ItemStack drop;
    private final EntityType entityType;
    private final Double unitPrice;
    private final Item menuItem;

}
