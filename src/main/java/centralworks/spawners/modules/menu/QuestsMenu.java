package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.models.quests.PlayerQuests;
import centralworks.spawners.modules.models.quests.cached.Quests;
import centralworks.spawners.modules.models.quests.suppliers.CraftQuest;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class QuestsMenu extends InventoryBuilder {

    public QuestsMenu(Player p, int page) {
        super(Main.get(), 6, "§8Missões");
        clear();
        setCancellable(true);
        final List<CraftQuest> quests = Quests.get().getList();
        setItem(4, getInfoPlayer(p));
        if (quests.size() > 0) {
            final List<Integer> slots = Lists.newArrayList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
            final double pages = Math.ceil(quests.size() / (0D + slots.size()));
            if (page > pages || page <= 0) {
                p.sendMessage("§cEssa página não existe ou ainda não foi configurada nenhuma missão.");
                return;
            }
            if (page != pages) {
                setItem(48, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eAvançar").lore("§7Clique para ir a página " + (page + 1)).setSkullUrl("http://textures.minecraft.net/texture/715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8").onClick(inventoryClickEvent -> new QuestsMenu(p, page + 1)));
            }
            if (page != 1) {
                setItem(50, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eVoltar").lore("§7Clique para ir a página " + (page - 1)).setSkullUrl("http://textures.minecraft.net/texture/eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1").onClick(inventoryClickEvent -> new QuestsMenu(p, page - 1)));
            }
            final List<CraftQuest> list = quests.subList(slots.size() * (page - 1), Math.min(quests.size(), slots.size() * (page + 1)));
            for (int count = 0; count < slots.size() && count < list.size(); count++) {
                final CraftQuest craftQuest = list.get(count);
                final Integer slot = slots.get(count);
                setItem(slot, new Item(craftQuest.getInventoryView().getAsItem(s -> s)).onClick(event -> new QuestMenu(p, craftQuest)));
            }
        }
        open(p);
    }

    public Item getInfoPlayer(Player p) {
        final PlayerQuests playerQuests = new PlayerQuests(p).query().persist();
        return new Item(Material.SKULL_ITEM, 1, (short)3).setSkullOwner(p.getName()).name("§eInformações").lore(
                "§fUsuário: §7" + p.getName(),
                "§fÚltima missão completada: §7" +
                        (playerQuests.hasCompletedQuest() ?
                                playerQuests.getLastCompleted().getSettings().getName().replace("&", "§") :
                                "Nenhuma"),
                "§fMissões completadas: §7" + playerQuests.getCompletedQuestsAmount(),
                "§fMissões em andamento: §7" + playerQuests.getQuestsInProgressAmount(),
                "§fMissões bloqueadas: §7" + playerQuests.getBlockedQuestsAmount()
        );
    }

}
