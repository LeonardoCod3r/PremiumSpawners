package centralworks.core.commons.cmds;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.lib.Configuration;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.enums.Permission;
import centralworks.lib.PlayerCommons;
import centralworks.core.commons.models.UserDetails;
import centralworks.core.commons.models.Limit;
import centralworks.core.commons.cache.LimitCached;
import centralworks.core.commons.models.enums.LimitType;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class LimitCommand extends BukkitCommand {

    private final Main plugin;

    public LimitCommand() {
        super("limit", "Sistema de limite de compras (Geradores)", "§c/limit help", Lists.newArrayList("limite"));
        this.plugin = Main.getInstance();
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        final Configuration messages = plugin.getMessages();
        if (args.length == 0) {
            if (s instanceof Player) {
                final Player p = (Player) s;
                final LoadingCache<String, UserDetails> cache = Caches.getCache(UserDetails.class);
                final UserDetails user = cache.getUnchecked(p.getName());
                p.sendMessage(messages.getMessage("limitView").replace("{type}", LimitType.BUY.getName()).replace("{limit}", BalanceFormatter.format(user.getBuyLimit())));
                p.sendMessage(messages.getMessage("limitView").replace("{type}", LimitType.SELL.getName()).replace("{limit}", BalanceFormatter.format(user.getSellLimit())));
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("ajuda") || args[0].equalsIgnoreCase("help")) {
                s.sendMessage("");
                s.sendMessage("§a/limite - Mostra seu limite de venda atual.");
                s.sendMessage("§a/limite ajuda - Mostra os comandos do sistema.");
                if (Permission.hasPermission(s, Permission.GIVE_LIMIT))
                    s.sendMessage("§a/limite give (player) (id) (amount)");
            } else Bukkit.dispatchCommand(s, "limite ajuda");
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("give")) {
                if (!Permission.hasPermission(s, Permission.GIVE_LIMIT)) {
                    s.sendMessage(messages.getMessage("permissionError"));
                    return true;
                }
                if (Bukkit.getPlayer(args[1]) == null) {
                    s.sendMessage(messages.getMessage("offlinePlayer"));
                    return true;
                }
                for (Limit limit : LimitCached.get().getList()) {
                    if (limit.getKey().equalsIgnoreCase(args[2])) {
                        if (StringUtils.isNumeric(args[3])) {
                            final int number = Integer.parseInt(args[3]);
                            s.sendMessage(messages.getMessage("limitGived").replace("{type}", limit.getLimitType().getName()).replace("{id}", limit.getKey()).replace("{player}", args[1]));
                            final Player player = Bukkit.getPlayer(args[1]);
                            new PlayerCommons(player).giveItem(limit.getItemStack().getAsItem(s1 -> s1), number);
                            return true;
                        } else s.sendMessage(messages.getMessage("invalidNumber"));
                        return true;
                    }
                }
                s.sendMessage(messages.getMessage("limitNotFound").replace("{id}", args[2]));
            } else Bukkit.dispatchCommand(s, "limitedecompra ajuda");
        }
        return true;
    }
}
