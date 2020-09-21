package centralworks.spawners.modules.models.quests;

import centralworks.spawners.modules.models.quests.cached.Quests;
import centralworks.spawners.modules.models.quests.suppliers.CraftQuest;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class QuestData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Expose
    private Long id;
    @Expose
    private String identifier;
    @Expose
    private boolean active = false;
    @Expose
    private Long startedAt = System.currentTimeMillis();
    @Expose(deserialize = false, serialize = false)
    @ManyToOne
    private PlayerQuests playerQuests;
    @OneToMany(mappedBy = "questData", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Expose
    private List<QuestRule> data = Lists.newLinkedList();

    public CraftQuest getQuest() {
        return Quests.get().get(craftQuest -> craftQuest.getSettings().getIdentifier().equals(identifier));
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) startedAt = System.currentTimeMillis();
    }

    public Optional<QuestRule> findRule(Predicate<QuestRule> questRulePredicate) {
        return data.stream().filter(questRulePredicate).findFirst();
    }

    public Optional<QuestRule> findRuleByInterpreter(String interpreter) {
        return data.stream().filter(questRule -> questRule.getInterpreter().equals(interpreter)).findFirst();
    }

    public List<QuestRule> findRulesByInterpreter(String interpreter) {
        return data.stream().filter(questRule -> questRule.getInterpreter().equals(interpreter)).collect(Collectors.toList());
    }

    public Optional<QuestRule> findRuleById(String id) {
        return data.stream().filter(questRule -> questRule.getId().equalsIgnoreCase(id)).findFirst();
    }

    public List<QuestRule> findRulesIfCompleted() {
        return data.stream().filter(QuestRule::isCompleted).collect(Collectors.toList());
    }

    public boolean isCompleted() {
        final boolean b = data.stream().allMatch(QuestRule::isCompleted);
        if (b) setActive(false);
        return b;
    }

}
