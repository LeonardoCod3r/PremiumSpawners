package centralworks.init;


import centralworks.Main;
import centralworks.core.commons.cache.LimitCached;
import centralworks.core.commons.models.Limit;
import com.google.gson.Gson;
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
    @Getter
    @Setter
    private boolean loaded;

    public static LimitLoader get() {
        return me == null ? me = new LimitLoader() : me;
    }

    public void run() {
        if (isLoaded()) return;
        final LimitCached cache = LimitCached.get();
        cache.clear();
        final File dir = new File(Main.getInstance().getDataFolder(), "limits");
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(file -> {
            try {
                final Limit limit = new Gson().fromJson(new FileReader(file), Limit.class);
                cache.add(limit);
            } catch (Exception ignored) {
            }
        });
        this.loaded = true;
    }

    public void setDefaults() {
        if (!Main.getInstance().getData().is("Answered")) {
            final File ctx = new File(Main.getInstance().getDataFolder(), "limits");
            try {
                if (!Main.getInstance().getDataFolder().exists()) Main.getInstance().getDataFolder().mkdir();
                if (!ctx.exists()) ctx.mkdir();
                final File file = new File(ctx, "examplelimit.json");
                if (!file.exists())
                    Files.copy(Main.getInstance().getClass().getResourceAsStream("/limits/examplelimit.json"), file.toPath());
            } catch (IOException ignored) {
            }
        }
    }
}
