package centralworks.hooks;

import centralworks.cache.google.Caches;
import centralworks.models.User;
import centralworks.lib.BalanceFormatter;
import com.google.common.cache.LoadingCache;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PlaceHolderHook extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "limite";
    }

    @Override
    public String getAuthor() {
        return "Leonardo";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, String identifier) {
        final LoadingCache<String, User> cache = Caches.getCache(User.class);
        final User user = cache.getIfPresent(p.getName());
        if (identifier.equals("compra")) return BalanceFormatter.format(user.getBuyLimit());
        if (identifier.equalsIgnoreCase("venda")) return BalanceFormatter.format(user.getSellLimit());
        return null;
    }
}
