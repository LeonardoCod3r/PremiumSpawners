package centralworks.market;

import centralworks.Main;
import centralworks.market.models.Market;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

public class MarketLoader {

    @Getter
    private static final MarketLoader instance;

    static {
        instance = new MarketLoader();
    }

    @Getter
    @Setter
    private boolean loaded;

    @SneakyThrows
    public void enable() {
        if (isLoaded()) return;
        val market = Market.getInstance();
        final File file = new File(Main.getInstance().getDataFolder(), "market.json");
        final Market copy = new Gson().fromJson(new FileReader(file), Market.class);
        market.setProducts(copy.getProducts());
        this.loaded = true;
    }

    public void load() {
        final File file = new File(Main.getInstance().getDataFolder(), "market.json");
        try {
            if (file.exists()) return;
            Files.copy(Main.getInstance().getClass().getResourceAsStream("/market.json"), file.toPath());
        } catch (IOException ignored) {
        }
    }

}
