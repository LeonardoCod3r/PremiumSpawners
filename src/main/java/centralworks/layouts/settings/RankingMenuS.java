package centralworks.layouts.settings;

import centralworks.lib.ItemSettings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class RankingMenuS extends InventorySettings {

    private List<Integer> slots;
    private ItemSettings player;
    private ItemSettings back;
    private ItemSettings updating;

}
