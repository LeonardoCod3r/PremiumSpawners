package centralworks.spawners.modules.menu.settings;

import centralworks.spawners.modules.models.ItemSettings;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class FriendsMenuS {

    private List<Integer> slots;
    private ItemSettings spawner_item;
    private ItemSettings back;
    private ItemSettings add;
    private ItemSettings player;
    private ItemSettings next_page;
    private ItemSettings back_page;

}
