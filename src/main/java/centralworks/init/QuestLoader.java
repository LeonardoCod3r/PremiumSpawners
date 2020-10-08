package centralworks.init;

import centralworks.Main;
import centralworks.core.quests.cache.Quests;
import centralworks.core.quests.other.CraftQuest;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class QuestLoader {

    private static QuestLoader me;
    @Getter
    @Setter
    private boolean loaded;

    public static QuestLoader get() {
        return me == null ? me = new QuestLoader() : me;
    }

    public void run() {
        if (isLoaded()) return;
        final Quests quests = Quests.get();
        quests.clear();
        final File dir = new File(Main.getInstance().getDataFolder(), "quests");
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(file -> {
            try {
                final CraftQuest craftQuest = new Gson().fromJson(new FileReader(file), CraftQuest.class);
                quests.add(craftQuest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        quests.setList(quests.getList().stream().sorted(Comparator.comparing(o -> o.getSettings().getPosition())).collect(Collectors.toList()));
        this.loaded = true;
    }

    public void setDefaults() {
        if (!Main.getInstance().getData().is("Answered")) {
            final File ctx = new File(Main.getInstance().getDataFolder(), "quests");
            try {
                if (!Main.getInstance().getDataFolder().exists()) Main.getInstance().getDataFolder().mkdir();
                if (!ctx.exists()) ctx.mkdir();
                File file = new File(ctx, "examplequest.json");
                if (!file.exists())
                    Files.copy(Main.getInstance().getClass().getResourceAsStream("/quests/examplequest.json"), file.toPath());
                file = new File(ctx, "otherquest.json");
                if (!file.exists())
                    Files.copy(Main.getInstance().getClass().getResourceAsStream("/quests/otherquest.json"), file.toPath());
            } catch (IOException ignored) {
            }
        }
    }
}
