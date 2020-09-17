package centralworks.spawners.modules.hook;

import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.modules.models.UserDetails;
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
        if (identifier.equals("compra")) {
            final UserDetails userDetails = new UserDetails(p).query().persist();
            return FormatBalance.format(userDetails.getBuyLimit());
        }
        if (identifier.equalsIgnoreCase("venda")) {
            final UserDetails userDetails = new UserDetails(p).query().persist();
            return FormatBalance.format(userDetails.getSellLimit());
        }
        return null;
    }
}
