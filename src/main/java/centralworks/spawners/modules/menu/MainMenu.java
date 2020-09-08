package centralworks.spawners.modules.menu;

import centralworks.spawners.Main;
import centralworks.spawners.lib.InventoryBuilder;
import centralworks.spawners.lib.Item;
import centralworks.spawners.modules.menu.settings.MainMenuS;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import org.bukkit.entity.Player;

public class MainMenu extends InventoryBuilder {

    public MainMenu(Player p) {
        super(Main.get(), 3, "§8Rede Nitro - Geradores");
        clear();
        setCancellable(true);

        final MainMenuS menu = MenusSettings.get().getMainMenuSettings();

        setItem(menu.getBuy().getItem_slot(), new Item(menu.getBuy().getAsItem(s -> s)).onClick(event -> new BuySpawnersMenu(p)));

        setItem(menu.getSpawners().getItem_slot(), new Item(menu.getSpawners().getAsItem(s -> s)).onClick(event -> new SpawnersMenu(p, 1)));

        open(p);
    }

}
