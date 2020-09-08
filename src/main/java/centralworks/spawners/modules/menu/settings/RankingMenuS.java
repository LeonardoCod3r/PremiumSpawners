package centralworks.spawners.modules.menu.settings;

import centralworks.spawners.modules.models.ItemSettings;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class RankingMenuS {

    private List<Integer> slots;
    private ItemSettings player;
    private ItemSettings back;
    private ItemSettings updating;

}
