package centralworks.quests.structure;

import centralworks.cache.simple.Quests;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CraftQuestSettings {

    private String name = "";
    private List<String> description = Lists.newArrayList();
    private String message = "";
    private String identifier;
    private Integer position;
    private boolean needPreviousQuestToRun;
    private List<String> needQuests;

    public String getName() {
        return hasName() ? name.replace("&", "§") : "§7Sem nome";
    }

    public List<String> getDescription() {
        return hasDescription() ? description.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList()) : Lists.newArrayList("§7Sem descrição");
    }

    public boolean hasDescription() {
        return !description.isEmpty();
    }

    public boolean hasName() {
        return !name.equals("");
    }

    public boolean hasMessage() {
        return !message.equals("");
    }

    public String getMessage() {
        return message.replace("&", "§");
    }

    public boolean hasPreviousQuest() {
        final int i = position - 1;
        return Quests.get().exists(craftQuest -> craftQuest.getSettings().getPosition() == i);
    }

    public CraftQuest getPreviousQuest() {
        final int i = position - 1;
        return Quests.get().get(craftQuest -> craftQuest.getSettings().getPosition() == i);
    }

    public boolean hasNeededQuests() {
        return !needQuests.isEmpty();
    }

    public List<CraftQuest> getNeededQuests() {
        return needQuests.stream().map(s -> Quests.get().get(craftQuest -> craftQuest.getSettings().getIdentifier().equals(s))).collect(Collectors.toList());
    }
}
