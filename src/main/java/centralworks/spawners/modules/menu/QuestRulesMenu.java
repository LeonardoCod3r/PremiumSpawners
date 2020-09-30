package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Date;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import centralworks.spawners.modules.models.quests.QuestData;
import centralworks.spawners.modules.models.quests.QuestRule;
import centralworks.spawners.modules.models.quests.suppliers.CraftQuest;
import centralworks.spawners.modules.models.quests.suppliers.CraftQuestRule;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class QuestRulesMenu extends InventoryBuilder {

    public QuestRulesMenu(Player p, CraftQuest cq, int page) {
        super(Main.getInstance(), 3, "§8Objetivos");
        clear();
        setCancellable(true);
        final PlayerQuests playerQuests = new PlayerQuests(p).query().persist();
        final QuestData data = playerQuests.findQuestByCraftQuest(cq).get();
        final List<QuestRule> rules = data.getData();
        final List<Integer> slots = Arrays.asList(11, 12, 13, 14, 15);
        final double pages = Math.ceil(rules.size() / (0D + slots.size()));
        if (page > pages || page <= 0) {
            p.sendMessage("§cEssa página não existe ou ainda não foi configurada nenhuma missão.");
            return;
        }
        if (page != pages) {
            setItem(18, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eAvançar").lore("§7Clique para ir a página " + (page + 1)).setSkullUrl("http://textures.minecraft.net/texture/715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8").onClick(inventoryClickEvent -> new QuestRulesMenu(p, cq, page + 1)));
        }
        if (page != 1) {
            setItem(26, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eVoltar").lore("§7Clique para ir a página " + (page - 1)).setSkullUrl("http://textures.minecraft.net/texture/eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1").onClick(inventoryClickEvent -> new QuestRulesMenu(p, cq, page - 1)));
        }
        final int calculate = page - 1;
        final List<QuestRule> list = rules.size() == 1 ? rules : rules.subList(calculate, Math.min(calculate + 5, rules.size()));
        for (int count = 0; count < list.size(); count++) {
            final QuestRule rule = list.get(count);
            final CraftQuestRule questRule = cq.getRules().stream().filter(craftQuestRule -> craftQuestRule.getId().equals(rule.getId())).findFirst().get();
            final Integer slot = slots.get(count);
            setItem(slot, new Item(questRule.getInventoryView().getAsItem(s -> s.replace("{value}", rule.getValueAsString()).replace("{completed}", rule.isCompleted() ? "§aSim" : "§cNão").replace("{completedDay}", rule.isCompleted() ? Date.getInstance().format(rule.getCompletedIn()) : "Nunca"))));
        }
        open(p);
    }
}
