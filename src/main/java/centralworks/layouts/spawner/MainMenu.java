package centralworks.layouts.spawner;

import centralworks.layouts.settings.MainMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import org.bukkit.entity.Player;

public class MainMenu extends InventoryMaker {

    public MainMenu(Player p) {
        super(3, "§8Rede Nitro - Geradores");
        clear();
        setCancellable(true);

        final MainMenuS menu = MenusSettings.get().getMainMenuSettings();

        setItem(menu.getBuy().getItem_slot(), new Item(menu.getBuy().getAsItem(s -> s)).onClick(event -> new BuySpawnersMenu(p)));

        setItem(menu.getSpawners().getItem_slot(), new Item(menu.getSpawners().getAsItem(s -> s)).onClick(event -> new SpawnersMenu(p, 1)));

        open(p);
    }

}
