package centralworks.init;

import centralworks.Main;
import centralworks.core.commons.cache.ICached;
import centralworks.core.commons.models.Impulse;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public class ImpulseLoader {

    private static ImpulseLoader me;
    @Getter
    @Setter
    private boolean loaded;

    public static ImpulseLoader get() {
        return me == null ? me = new ImpulseLoader() : me;
    }

    public void run() {
        if (isLoaded()) return;
        final ICached cache = ICached.get();
        cache.clear();
        final File dir = new File(Main.getInstance().getDataFolder(), "impulses");
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(file -> {
            try {
                final Impulse impulse = new Gson().fromJson(new FileReader(file), Impulse.class);
                cache.add(impulse);
            } catch (Exception ignored) {
            }
        });
        this.loaded = true;
    }

    public void setDefaults() {
        if (!Main.getInstance().getData().is("Answered")) {
            final File ctx = new File(Main.getInstance().getDataFolder(), "impulses");
            try {
                if (!Main.getInstance().getDataFolder().exists()) Main.getInstance().getDataFolder().mkdir();
                if (!ctx.exists()) ctx.mkdir();
                final File file = new File(ctx, "exampleimpulse.json");
                if (!file.exists())
                    Files.copy(Main.getInstance().getClass().getResourceAsStream("/impulses/exampleimpulse.json"), file.toPath());
            } catch (IOException ignored) {
            }
        }
    }

}
