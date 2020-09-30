package centralworks.spawners.modules.cmds;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.BalanceFormatter;
import centralworks.spawners.lib.enums.Permission;
import centralworks.spawners.modules.models.PlayerCommons;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.addons.Limit;
import centralworks.spawners.modules.models.addons.LimitCached;
import centralworks.spawners.modules.models.addons.LimitType;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class LimitCommand extends BukkitCommand {

    private Main plugin;

    @Inject
    public LimitCommand(Main plugin) {
        super("limit", "Sistema de limite de compras (Geradores)", "§c/limit help", Lists.newArrayList("limite"));
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        final Configuration messages = plugin.getMessages();
        if (args.length == 0) {
            if (s instanceof Player) {
                final Player p = (Player) s;
                final UserDetails user = new UserDetails(p).query().persist();
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
