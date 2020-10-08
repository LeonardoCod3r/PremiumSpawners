package centralworks.layouts.settings;

import centralworks.lib.ItemSettings;
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
