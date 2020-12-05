package centralworks.layouts.settings;

import centralworks.lib.ItemSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class BuySpawnersMenuS extends InventorySettings {

    private ItemSettings comingSoon;
    private ItemSettings spawnerItem;
    private ItemSettings info;
    private List<Integer> slots;

}
