package centralworks.layouts.menus.spawner;

import centralworks.layouts.settings.MainMenuS;
import centralworks.layouts.settings.MenusSettings;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import org.bukkit.entity.Player;

public class MainMenu extends InventoryMaker {

    private static final MainMenuS menu;

    static {
        menu = MenusSettings.get().getMainMenuSettings();
    }

    public MainMenu(Player p) {
        super(menu.getRows(), menu.getName());
        if (!menu.isToggle()) return;
        clear();
        setCancellable(true);

        setItem(menu.getBuy().getItem_slot(), new Item(menu.getBuy().getAsItem(s -> s)).onClick(event -> new BuySpawnersMenu(p)));
        setItem(menu.getSpawners().getItem_slot(), new Item(menu.getSpawners().getAsItem(s -> s)).onClick(event -> new SpawnersMenu(p, 1)));

        open(p);
    }

}
