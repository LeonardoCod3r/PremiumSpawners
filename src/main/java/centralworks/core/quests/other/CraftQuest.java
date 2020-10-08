package centralworks.core.quests.other;

import centralworks.lib.ItemSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CraftQuest {

    private CraftQuestSettings settings;
    private List<CraftQuestRule> rules;
    private List<CraftReward> rewards;
    private ItemSettings inventoryView;


}
