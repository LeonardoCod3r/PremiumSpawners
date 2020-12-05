package centralworks.layouts.settings;

import centralworks.lib.ItemSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class BoosterMenuS extends InventorySettings {

    private ItemSettings add;
    private ItemSettings boosters;
    private ItemSettings back;

}
