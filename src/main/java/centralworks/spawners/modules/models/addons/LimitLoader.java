package centralworks.spawners.modules.models.addons;


import centralworks.spawners.Main;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class LimitLoader {

    private static LimitLoader me;

    public static LimitLoader get() {
        return me == null ? me = new LimitLoader() : me;
    }

    @Getter
    @Setter
    private boolean loaded;

    public void run() {
        if (isLoaded()) return;
        final LimitCached cache = LimitCached.get();
        cache.clear();
        final File dir = new File(Main.get().getDataFolder(), "limits");
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(file -> {
            try {
                final Limit limit = Main.getGson().fromJson(new FileReader(file), Limit.class);
                cache.add(limit);
            } catch (Exception ignored) {
            }
        });
        this.loaded = true;
    }

    public void setDefaults() {
        if (!Main.getData().is("Answered")) {
            final File ctx = new File(Main.get().getDataFolder(), "limits");
            try {
                if (!Main.get().getDataFolder().exists()) Main.get().getDataFolder().mkdir();
                if (!ctx.exists()) ctx.mkdir();
                final File file = new File(ctx, "examplelimit.json");
                if (!file.exists())
                    Files.copy(Main.get().getClass().getResourceAsStream("/limits/examplelimit.json"), file.toPath());
            } catch (IOException ignored) {
            }
        }
    }
}
