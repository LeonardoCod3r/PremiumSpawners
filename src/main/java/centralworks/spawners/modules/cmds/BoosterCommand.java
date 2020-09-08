package centralworks.spawners.modules.cmds;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.Permission;
import centralworks.spawners.modules.models.PlayerCommons;
import centralworks.spawners.modules.models.addons.ICached;
import centralworks.spawners.modules.models.addons.Impulse;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class BoosterCommand extends BukkitCommand {

    public BoosterCommand() {
        super("booster", "Comandos do sistema de impulsos para Spawners.", "§c/booster help", Lists.newArrayList("multiplicador", "boosters"));
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        final Configuration messages = Main.getMessages();
        if (s instanceof Player && args.length == 0) {
            final Player p = (Player) s;
            final DropStorage dropStorage = new DropStorage(p.getName()).query().persist();
            p.sendMessage(messages.getMessage("booster-view").replace("{multiplier}", dropStorage.getMultiplier().toString()));
            return true;
        }
        if (!Permission.hasPermission(s, Permission.GIVE_BOOSTER)) {
            s.sendMessage(messages.getMessage("permissionError"));
            return true;
        }
        if (args.length != 4 || !args[0].equalsIgnoreCase("give")) {
            s.sendMessage("§cComando incorreto. Utilize: /booster give (jogador) (booster) (quantidade).");
            return true;
        }
        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            s.sendMessage(messages.getMessage("offlinePlayer").replace("{player}", args[1]));
            return true;
        }
        final ICached cache = ICached.get();
        if (!cache.exists(q -> q.getId().equalsIgnoreCase(args[2]))) {
            s.sendMessage(messages.getMessage("boosterNotFound").replace("{id}", args[2]));
            return true;
        }
        try {
            final int i = Integer.parseInt(args[3]);
            final Impulse impulseFork = cache.get(q -> q.getId().equalsIgnoreCase(args[2]));
            new PlayerCommons(Bukkit.getPlayer(args[1])).giveItem(impulseFork.getAsItem(), i);
            s.sendMessage(messages.getMessage("deliveredBooster").replace("{amount}", "" + i).replace("{player}", args[1]));
        } catch (Exception e) {
        }
        return true;
    }
}
