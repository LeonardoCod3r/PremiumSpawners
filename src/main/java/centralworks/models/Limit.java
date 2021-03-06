package centralworks.models;

import centralworks.lib.ItemSettings;
import centralworks.models.enums.LimitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Limit {

    private String key;
    private LimitType limitType;
    private ItemSettings itemStack;
    private Double value;
    private String permission;

    public boolean hasPermission(Player player) {
        if (!hasPermission()) return true;
        return player.hasPermission(permission);
    }

    public boolean hasPermission() {
        return permission.equalsIgnoreCase("");
    }

}
