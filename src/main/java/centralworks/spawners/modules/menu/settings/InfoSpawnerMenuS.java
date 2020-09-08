package centralworks.spawners.modules.menu.settings;

import centralworks.spawners.modules.models.ItemSettings;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class InfoSpawnerMenuS {

    private ItemSettings spawner_item;
    private ItemSettings boosters;
    private ItemSettings friends;
    private ItemSettings utils;
    private ItemSettings top;

}
