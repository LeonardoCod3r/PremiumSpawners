package centralworks.hooks;

import centralworks.cache.Caches;
import centralworks.lib.BalanceFormatter;
import centralworks.core.commons.models.UserDetails;
import com.google.common.cache.LoadingCache;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
        final LoadingCache<String, UserDetails> cache = Caches.getCache(UserDetails.class);
        final UserDetails user = cache.getUnchecked(p.getName());
        if (identifier.equals("compra")) return BalanceFormatter.format(user.getBuyLimit());
        if (identifier.equalsIgnoreCase("venda")) return BalanceFormatter.format(user.getSellLimit());
        return null;
    }
}
