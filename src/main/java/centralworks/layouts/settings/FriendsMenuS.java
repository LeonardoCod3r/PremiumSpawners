package centralworks.layouts.settings;

import centralworks.lib.ItemSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class FriendsMenuS extends InventorySettings{

    private List<Integer> slots;
    private ItemSettings spawner_item;
    private ItemSettings back;
    private ItemSettings add;
    private ItemSettings player;
    private ItemSettings next_page;
    private ItemSettings back_page;

}
