package centralworks.spawners.modules.menu.settings;

import centralworks.spawners.modules.models.ItemSettings;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UtilitiesMenuS {

    private ItemSettings add;
    private ItemSettings get;
    private ItemSettings remove;
    private ItemSettings back;

}
