package centralworks.spawners.modules.cmds;

import centralworks.spawners.Main;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.EntityName;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.lib.Permission;
import centralworks.spawners.modules.menu.BuySpawnersMenu;
import centralworks.spawners.modules.menu.MainMenu;
import centralworks.spawners.modules.menu.SpawnersMenu;
import centralworks.spawners.modules.models.spawners.SpawnerItem;
import centralworks.spawners.modules.models.spawners.cached.SICached;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnersCommand extends BukkitCommand {

    public SpawnersCommand() {
        super("spawners", "Comandos do sistema de spawners", "§c/spawners", Lists.newArrayList("geradores", "spawner", "gerador"));
    }

    @Override
    public boolean execute(CommandSender s, String cmd, String[] args) {
        final Configuration messages = Main.getMessages();
        final Configuration configSpawners = Main.getSpawners();
        switch (args.length) {
            case 0:
                if (s instanceof Player) new MainMenu(((Player) s));
                break;
            case 1:
                if (s instanceof Player) {
                    final Player p = ((Player) s);
                    if (args[0].equalsIgnoreCase("comprar")) new BuySpawnersMenu(p);
                    else if (args[0].equalsIgnoreCase("manager")) new SpawnersMenu(p, 1);
                    else if (args[0].equalsIgnoreCase("reload")) {
                        if (!Permission.hasPermission(s, Permission.ADMIN)) {
                            s.sendMessage(messages.getMessage("permissionError"));
                            return true;
                        }
                        Main.get().reload();
                    }
                    else new MainMenu(((Player) s));
                }
                break;
            case 5:
                if (!Permission.hasPermission(s, Permission.GIVE_SPAWNER)) {
                    s.sendMessage(messages.getMessage("permissionError"));
                    return true;
                }
                if (!args[0].equalsIgnoreCase("give") && !args[0].equalsIgnoreCase("entregar") && !args[0].equalsIgnoreCase("send")) {
                    s.sendMessage("§cComando incorreto. Utilize /spawner give (jogador) (tipo) (pilha) (stack).");
                    return true;
                }
                if (!Bukkit.getOfflinePlayer(args[1]).isOnline()) {
                    s.sendMessage(messages.getMessage("offlinePlayer").replace("{player}", args[1]));
                    return true;
                }
                if (configSpawners.section("List").stream().noneMatch(s1 -> s1.equalsIgnoreCase(args[2]))) {
                    s.sendMessage(messages.getMessage("spawnerDisabled").replace("{entity-type}", args[2]));
                    return true;
                }
                if (!StringUtils.isNumeric(args[3]) && Double.parseDouble(args[3]) < 1) {
                    s.sendMessage(messages.getMessage("invalidNumber").replace("{number}", args[3]));
                    return true;
                }
                if (!StringUtils.isNumeric(args[4]) && Integer.parseInt(args[4]) < 1) {
                    s.sendMessage(messages.getMessage("invalidNumber").replace("{number}", args[4]));
                    return true;
                }
                try {
                    final SICached siCached = SICached.get();
                    final int i = Integer.parseInt(args[4]);
                    final SpawnerItem spawnerItem = siCached.get(spawnerItemFork -> spawnerItemFork.getEntityType().equals(EntityType.valueOf(args[2]))).getSpawnerItem();
                    spawnerItem.setAmountSpawners(Double.valueOf(args[3]));
                    spawnerItem.setAmountItem(i);
                    spawnerItem.giveItem(Bukkit.getPlayer(args[1]));
                    s.sendMessage(messages.getMessage("deliveredSpawner")
                            .replace("{amount}", "" + i)
                            .replace("{entity-type}", EntityName.valueOf(spawnerItem.getEntityType()).getName())
                            .replace("{stack}", FormatBalance.format(spawnerItem.getAmountSpawners()))
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getConsoleSender().sendMessage("§cOcorreu um erro ao entregar o spawner, verifique o console.");
                }
                break;
            default:
                s.sendMessage(getUsage());
        }
        return true;
    }
}
