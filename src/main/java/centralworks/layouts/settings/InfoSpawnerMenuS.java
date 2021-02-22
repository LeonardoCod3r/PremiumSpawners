package centralworks.layouts.settings;

import centralworks.lib.ItemSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class InfoSpawnerMenuS extends InventorySettings {

    private ItemSettings spawner_item;
    private ItemSettings boosters;
    private ItemSettings friends;
    private ItemSettings utils;
    private ItemSettings top;

}
