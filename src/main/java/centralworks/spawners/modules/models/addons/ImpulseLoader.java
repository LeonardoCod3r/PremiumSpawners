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

public class ImpulseLoader {

    private static ImpulseLoader me;

    public static ImpulseLoader get() {
        return me == null ? me = new ImpulseLoader() : me;
    }

    @Getter
    @Setter
    private boolean loaded;

    public void run() {
        if (isLoaded()) return;
        final ICached cache = ICached.get();
        cache.clear();
        final File dir = new File(Main.get().getDataFolder(), "impulses");
        Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(file -> {
            try {
                final Impulse impulse = Main.getGson().fromJson(new FileReader(file), Impulse.class);
                cache.add(impulse);
            } catch (Exception ignored) {
            }
        });
        this.loaded = true;
    }

    public void setDefaults() {
        if (!Main.getData().is("Answered")) {
            final File ctx = new File(Main.get().getDataFolder(), "impulses");
            try {
                if (!Main.get().getDataFolder().exists()) Main.get().getDataFolder().mkdir();
                if (!ctx.exists()) ctx.mkdir();
                final File file = new File(ctx, "exampleimpulse.json");
                if (!file.exists())
                    Files.copy(Main.get().getClass().getResourceAsStream("/impulses/exampleimpulse.json"), file.toPath());
            } catch (IOException ignored) {
            }
        }
    }

}
