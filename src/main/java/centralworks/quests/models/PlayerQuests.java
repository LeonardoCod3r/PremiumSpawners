package centralworks.quests.models;

import centralworks.cache.simple.Quests;
import centralworks.quests.structure.CraftQuest;
import centralworks.quests.structure.CraftQuestSettings;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Data
public class PlayerQuests implements Serializable {

    @Id
    @Column(length = 16)
    @Expose
    private String name;
    @Expose
    private String lastCompletedId = "";
    @OneToMany(mappedBy = "playerQuests", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Expose
    private List<QuestData> compounds = Lists.newLinkedList();

    public PlayerQuests(OfflinePlayer player) {
        this.name = player.getName();
    }

    public PlayerQuests(String name) {
        this.name = name;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(name);
    }

    public boolean hasCompletedQuest() {
        return !lastCompletedId.equals("");
    }

    public CraftQuest getLastCompleted() {
        return Quests.get().get(craftQuest -> craftQuest.getSettings().getIdentifier().equals(lastCompletedId));
    }

    public Integer getBlockedQuestsAmount() {
        return (int) compounds.stream().filter(questData -> !questData.isActive()).count();
    }

    public List<QuestData> getBlockedQuests() {
        return compounds.stream().filter(questData -> !questData.isActive()).collect(Collectors.toList());
    }

    public Integer getCompletedQuestsAmount() {
        return (int) compounds.stream().filter(QuestData::isCompleted).count();
    }

    public Integer getQuestsInProgressAmount() {
        return (int) compounds.stream().filter(QuestData::isActive).count();
    }

    public List<QuestData> getQuestsInProgress() {
        return compounds.stream().filter(QuestData::isActive).collect(Collectors.toList());
    }

    public List<QuestData> getQuestsCompleted() {
        return compounds.stream().filter(QuestData::isCompleted).collect(Collectors.toList());
    }

    public void addQuestsDefaults() {
        for (CraftQuest quest : Quests.get().getList()) {
            final CraftQuestSettings settings = quest.getSettings();
            final String id = settings.getIdentifier();
            final QuestData data = new QuestData();
            data.setPlayerQuests(this);
            data.setActive(settings.getPosition() == 1 || (!settings.isNeedPreviousQuestToRun() && settings.getNeedQuests().isEmpty()));
            data.setIdentifier(id);
            data.setData(quest.getRules().stream().map(craftQuestRule -> {
                final QuestRule rule = new QuestRule();
                rule.setQuestData(data);
                rule.setId(craftQuestRule.getId());
                rule.setValue(craftQuestRule.getDefaultValue());
                rule.setInterpreter(craftQuestRule.getType());
                return rule;
            }).collect(Collectors.toList()));
            addQuest(data);
        }
        update();
    }

    private void update() {
        getCompounds().forEach(q -> {
            final CraftQuest craftQuests = q.getQuest();
            final List<String> needQuests = craftQuests.getSettings().getNeedQuests();
            if (!q.isActive() && !q.isCompleted() && needQuests.stream().map(this::findQuestById).allMatch(questData -> questData.get().isCompleted())) {
                if (needQuests.stream().map(this::findQuestById).allMatch(questData -> questData.get().isCompleted()) && !q.getQuest().getSettings().isNeedPreviousQuestToRun())
                    q.setActive(true);
            }
        });
    }

    public void nextQuest(QuestData previous) {
        final CraftQuest quest = previous.getQuest();
        quest.getRewards().forEach(craftReward -> craftReward.toAward(getName()));
        final CraftQuestSettings settings = quest.getSettings();
        if (settings.hasMessage() && getOfflinePlayer().isOnline()) getPlayer().sendMessage(settings.getMessage());
        addQuestsDefaults();
        final List<CraftQuest> quests = Quests.get().getList();
        final int i = quests.indexOf(previous.getQuest()) + 1;
        if (quests.size() > i) {
            final CraftQuest craftQuest = quests.get(i);
            final Optional<QuestData> next = findQuestById(craftQuest.getSettings().getIdentifier());
            next.ifPresent(q -> {
                if (craftQuest.getSettings().isNeedPreviousQuestToRun() && !q.isActive() && !q.isCompleted())
                    q.setActive(true);
            });
        }
        update();
    }

    public void addQuest(String id, List<QuestRule> questRules, Boolean... isActive) {
        final QuestData questData = new QuestData();
        questData.setPlayerQuests(this);
        questData.setIdentifier(id);
        questData.setActive(isActive.length > 0 && isActive[0]);
        questData.setData(Lists.newLinkedList(questRules));
        questData.getData().forEach(questRule -> questRule.setQuestData(questData));
        compounds.add(questData);
    }

    public void addQuest(QuestData questData) {
        if (compounds.stream().noneMatch(questData1 -> questData1.getIdentifier().equals(questData.getIdentifier())))
            compounds.add(questData);
    }

    public void removeQuest(String id) {
        compounds.removeIf(questData -> questData.getIdentifier().equalsIgnoreCase(id));
    }

    public void removeQuest(QuestData questData) {
        compounds.remove(questData);
    }

    public void removeQuestIf(Predicate<QuestData> questDataPredicate) {
        compounds.removeIf(questDataPredicate);
    }

    public Optional<QuestData> findQuestById(String id) {
        return compounds.stream().filter(questData -> questData.getIdentifier().equals(id)).findFirst();
    }

    public Optional<QuestData> findQuestByCraftQuest(CraftQuest craftQuest) {
        return findQuestById(craftQuest.getSettings().getIdentifier());
    }

    public Optional<QuestData> findQuestBy(Predicate<QuestData> questDataPredicate) {
        return compounds.stream().filter(questDataPredicate).findFirst();
    }

    public Optional<QuestData> findQuestByInterpreter(String interpreter) {
        return compounds.stream().filter(questData -> questData.findRuleByInterpreter(interpreter).isPresent()).findFirst();
    }

    public List<QuestData> findQuestsByInterpreter(String interpreter) {
        return compounds.stream().filter(questData -> questData.findRuleByInterpreter(interpreter).isPresent()).collect(Collectors.toList());
    }

}
