package centralworks.spawners.modules.models.quests.suppliers;

import centralworks.spawners.Main;
import centralworks.spawners.modules.models.quests.cached.Quests;
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
        final File dir = new File(Main.get().getDataFolder(), "quests");
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(file -> {
            try {
                final CraftQuest craftQuest = Main.getGson().fromJson(new FileReader(file), CraftQuest.class);
                quests.add(craftQuest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        quests.setList(quests.getList().stream().sorted(Comparator.comparing(o -> o.getSettings().getPosition())).collect(Collectors.toList()));
        this.loaded = true;
    }

    public void setDefaults() {
        if (!Main.getData().is("Answered")) {
            final File ctx = new File(Main.get().getDataFolder(), "quests");
            try {
                if (!Main.get().getDataFolder().exists()) Main.get().getDataFolder().mkdir();
                if (!ctx.exists()) ctx.mkdir();
                final File file = new File(ctx, "examplequest.json");
                if (!file.exists())
                    Files.copy(Main.get().getClass().getResourceAsStream("/quests/examplequest.json"), file.toPath());
            } catch (IOException ignored) {
            }
        }
    }
}
