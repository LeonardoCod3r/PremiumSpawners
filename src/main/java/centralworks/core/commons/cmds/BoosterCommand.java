package centralworks.core.commons.cmds;

import centralworks.Main;
import centralworks.cache.Caches;
import centralworks.core.commons.cache.ICached;
import centralworks.core.commons.models.Impulse;
import centralworks.core.dropstorage.models.DropStorage;
import centralworks.lib.PlayerCommons;
import centralworks.lib.Settings;
import centralworks.lib.enums.Permission;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class BoosterCommand extends BukkitCommand {

    private final Main plugin;

    public BoosterCommand() {
        super("booster", "Comandos do sistema de impulsos para Spawners.", "§c/booster help", Lists.newArrayList("multiplicador", "boosters"));
        this.plugin = Main.getInstance();
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        final Settings.Navigate nav = plugin.getMessages().navigate();
        if (s instanceof Player && args.length == 0) {
            final Player p = (Player) s;
            final LoadingCache<String, DropStorage> cache = Caches.getCache(DropStorage.class);
            final DropStorage dropStorage = cache.getIfPresent(p.getName());
            p.sendMessage(nav.getMessage("booster-view").replace("{multiplier}", dropStorage.getMultiplier().toString()));
            return true;
        }
        if (!Permission.hasPermission(s, Permission.GIVE_BOOSTER)) {
            s.sendMessage(nav.getMessage("permissionError"));
            return true;
        }
        if (args.length != 4 || !args[0].equalsIgnoreCase("give")) {
            s.sendMessage("§cComando incorreto. Utilize: /booster give (jogador) (booster) (quantidade).");
            return true;
        }
        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            s.sendMessage(nav.getMessage("offlinePlayer").replace("{player}", args[1]));
            return true;
        }
        final ICached cache = ICached.get();
        if (!cache.exists(q -> q.getId().equalsIgnoreCase(args[2]))) {
            s.sendMessage(nav.getMessage("boosterNotFound").replace("{id}", args[2]));
            return true;
        }
        try {
            final int i = Integer.parseInt(args[3]);
            final Impulse impulseFork = cache.get(q -> q.getId().equalsIgnoreCase(args[2]));
            new PlayerCommons(Bukkit.getPlayer(args[1])).giveItem(impulseFork.getAsItem(), i);
            s.sendMessage(nav.getMessage("deliveredBooster").replace("{amount}", "" + i).replace("{player}", args[1]));
        } catch (Exception ignored) {
        }
        return true;
    }
}
