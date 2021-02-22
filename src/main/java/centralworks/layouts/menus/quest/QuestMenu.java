package centralworks.layouts.menus.quest;

import centralworks.cache.google.Caches;
import centralworks.lib.Date;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import centralworks.quests.models.PlayerQuests;
import centralworks.quests.models.QuestData;
import centralworks.quests.structure.CraftQuest;
import centralworks.quests.structure.CraftQuestSettings;
import com.google.common.cache.LoadingCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class QuestMenu extends InventoryMaker {

    public QuestMenu(Player p, CraftQuest cq) {
        super(3, cq.getSettings().getName());
        clear();
        setCancellable(true);
        final LoadingCache<String, PlayerQuests> cache = Caches.getCache(PlayerQuests.class);
        final PlayerQuests playerQuests = cache.getIfPresent(p.getName());
        playerQuests.findQuestByCraftQuest(cq).ifPresent(questData -> {
            setItem(10, getCraftQuestInfoItem(cq));
            setItem(13, getCraftQuestStatusItem(playerQuests, cq));
            setItem(15, getRulesItem(p, cq));
            open(p);
        });
    }

    public Item getCraftQuestInfoItem(CraftQuest cq) {
        final CraftQuestSettings se = cq.getSettings();
        return new Item(Material.getMaterial("SKULL_ITEM"), 1, (short) 3)
                .setSkullUrl("http://textures.minecraft.net/texture/d01afe973c5482fdc71e6aa10698833c79c437f21308ea9a1a095746ec274a0f")
                .name("§7" + se.getName())
                .lore(se.getDescription());
    }

    public Item getCraftQuestStatusItem(PlayerQuests playerQuests, CraftQuest cq) {
        final CraftQuestSettings se = cq.getSettings();
        final List<String> neededQuestsText = se.hasNeededQuests() ? se.getNeededQuests().stream().map(craftQuest -> {
            final QuestData data = playerQuests.findQuestByCraftQuest(craftQuest).get();
            return "  §f- " + craftQuest.getSettings().getName() + "§7: " + (data.isCompleted() ? "§aCompleta" : data.isActive() ? "§eEm andamento" : "§cBloqueada");
        }).collect(Collectors.toList()) : null;
        final QuestData data = playerQuests.findQuestByCraftQuest(cq).get();
        return new Item(Material.getMaterial("SKULL_ITEM"), 1, (short) 3)
                .name("§eInformações: ")
                .setSkullUrl("http://textures.minecraft.net/texture/3937edcf2c2976fd4617b99f363d3d62832ab9489a2709dee274f799e2983688")
                .lore("§fEstá bloqueada: " + (data.isActive() ? "§cNão" : "§aSim"),
                        "§fData de início: §7" + (!data.isActive() ? "Nunca" : Date.getInstance().format(data.getStartedAt())),
                        "§fFoi completada: " + (data.isCompleted() ? "§aSim" : "§cNão"),
                        "§fNecessita da missão anterior: " + (se.isNeedPreviousQuestToRun() ? "§aSim" : "§cNão"),
                        "§fNecessita de outras missões: " + (se.hasNeededQuests() ? "§aSim" : "§cNão")
                ).addLines(neededQuestsText);
    }

    public Item getRulesItem(Player p, CraftQuest cq) {
        return new Item(Material.getMaterial("SKULL_ITEM"), 1, (short) 3)
                .setSkullUrl("http://textures.minecraft.net/texture/9dca37c95fad58929e47e95796815cd681ff161438e05d261423811597aeb1b4")
                .name("§bObjetivos")
                .lore("§7Clique para acessar os objetivos dessa missão.").onClick(event -> new QuestRulesMenu(p, cq, 1));
    }


}
