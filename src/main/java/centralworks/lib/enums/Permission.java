package centralworks.lib.enums;

import org.bukkit.command.CommandSender;

public enum Permission {

    ADMIN("spawners.admin"),
    GIVE_SPAWNER("spawners.give"),
    GIVE_BOOSTER("booster.give"),
    GIVE_LIMIT("limit.give"),
    SELL_ALL("armazem.sellall"),
    AUTO_SELL("armazem.autosell");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public static boolean hasPermission(CommandSender cs, Permission permission) {
        return permission.hasPermission(cs);
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission(CommandSender cs) {
        return cs.hasPermission(ADMIN.getPermission()) || cs.hasPermission(getPermission());
    }
}
