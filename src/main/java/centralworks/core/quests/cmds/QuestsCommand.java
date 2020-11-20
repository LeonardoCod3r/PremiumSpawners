package centralworks.core.quests.cmds;

import centralworks.layouts.quest.QuestsMenu;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class QuestsCommand extends BukkitCommand {

    public QuestsCommand() {
        super("quests", "Menu de missões", "§c/missoes", Lists.newArrayList("missoes"));
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        if (s instanceof Player) {
            new QuestsMenu(((Player) s), 1);
        }
        return true;
    }
}
