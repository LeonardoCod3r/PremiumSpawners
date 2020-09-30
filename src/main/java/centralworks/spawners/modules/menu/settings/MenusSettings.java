package centralworks.spawners.modules.menu.settings;

import centralworks.spawners.Main;
import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;

@Data
@RequiredArgsConstructor
public class MenusSettings {

    private static MenusSettings menusSettings;

    private InfoSpawnerMenuS infoSpawnerMenuSettings;
    private UtilitiesMenuS utilsMenuSettings;
    private FriendsMenuS friendsMenuSettings;
    private RankingMenuS rankingMenuSettings;
    private BoosterMenuS boosterMenuSettings;
    private MainMenuS mainMenuSettings;

    public static MenusSettings get() {
        if (menusSettings == null) {
            try {
                final File file = new File(Main.getInstance().getDataFolder(), "inventory.json");
                if (!file.exists())
                    Files.copy(Main.getInstance().getClass().getResourceAsStream("/inventory.json"), file.toPath());
                final FileReader fileReader = new FileReader(new File(Main.getInstance().getDataFolder(), "inventory.json"));
                menusSettings = new Gson().fromJson(fileReader, MenusSettings.class);
            } catch (Exception ignored) {
            }
        }
        return menusSettings;
    }

    public static MenusSettings newInstance() {
        try {
            final File file = new File(Main.getInstance().getDataFolder(), "inventory.json");
            if (!file.exists())
                Files.copy(Main.getInstance().getClass().getResourceAsStream("/inventory.json"), file.toPath());
            final FileReader fileReader = new FileReader(new File(Main.getInstance().getDataFolder(), "inventory.json"));
            menusSettings = new Gson().fromJson(fileReader, MenusSettings.class);
        } catch (Exception ignored) {
        }
        return menusSettings;
    }
}
