package centralworks.spawners.modules.menu.settings;

import centralworks.spawners.modules.models.ItemSettings;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MainMenuS {

    private ItemSettings buy;
    private ItemSettings spawners;

}
