package centralworks.layouts;

import centralworks.Main;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.InventoryBuilder;
import centralworks.lib.Item;
import centralworks.layouts.settings.InfoSpawnerMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.spawners.models.Spawner;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnersMenu extends InventoryBuilder {

    public SpawnersMenu(Player p, int page) {
        super(Main.getInstance(), 3, "§8Seus geradores");
        final InfoSpawnerMenuS mainMenuS = MenusSettings.get().getInfoSpawnerMenuSettings();
        clear();
        setCancellable(true);
        final UserDetails user = new UserDetails(p).query().persist();
        user.getSpawners(spawners -> {
            if (!spawners.isEmpty()) {
                final List<Integer> slots = Lists.newArrayList(11, 12, 13, 14, 15);
                final double pages = Math.ceil(spawners.size() / 5.0);
                if (page > pages || page <= 0) {
                    p.sendMessage("§cEssa página não existe.");
                    return;
                }
                if (page != pages) {
                    setItem(16, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eAvançar").lore("§7Clique para ir a página " + (page + 1)).setSkullUrl("http://textures.minecraft.net/texture/715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8").onClick(inventoryClickEvent -> new SpawnersMenu(p, page + 1)));
                }
                if (page != 1) {
                    setItem(10, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eVoltar").lore("§7Clique para ir a página " + (page - 1)).setSkullUrl("http://textures.minecraft.net/texture/eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1").onClick(inventoryClickEvent -> new SpawnersMenu(p, page - 1)));
                }
                final int calculate = page - 1;
                final List<Spawner> subListSpawners = spawners.size() == 1 ? spawners : spawners.subList(calculate, Math.min(calculate + 5, spawners.size()));
                int count = 0;
                for (Spawner spawner : subListSpawners) {
                    setItem(slots.get(count), new Item(mainMenuS.getSpawner_item().getAsItem(s -> s
                            .replace("{coins}", BalanceFormatter.format(spawner.getPriceAll()))
                            .replace("{owner}", spawner.getOwner())
                            .replace("{entity-type}", spawner.getEntityName())
                            .replace("{stack}", BalanceFormatter.format(spawner.getAmount()))
                            .replace("{location}", "x: " + spawner.getLocation().getBlockX() + " y: " + spawner.getLocation().getBlockY() + " z: " + spawner.getLocation().getBlockZ()))
                    ).name("§aGerador §f#" + (spawners.indexOf(spawner) + 1)).onClick(event -> new InfoSpawnerMenu(spawner, p)));
                    count++;
                }
            } else
                setItem(13, new Item(Material.WEB).name("§cSem spawners.").lore("§7Você não possui nenhum spawner.", "§7Clique para poder comprar algum.").onClick(inventoryClickEvent -> new BuySpawnersMenu(p)));
        });
        open(p);
    }
}
