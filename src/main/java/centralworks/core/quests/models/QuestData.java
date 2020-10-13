package centralworks.core.quests.models;

import centralworks.core.quests.cache.Quests;
import centralworks.core.quests.other.CraftQuest;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class QuestData implements Serializable {

    @Id
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Expose
    private String identifier;
    @Expose
    private boolean active = false;
    @Expose
    private Long startedAt = System.currentTimeMillis();
    @ManyToOne
    @Getter(AccessLevel.PRIVATE)
    @Setter
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
