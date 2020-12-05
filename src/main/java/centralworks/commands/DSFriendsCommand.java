package centralworks.commands;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.DropStorage;
import centralworks.lib.Settings;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DSFriendsCommand extends BukkitCommand {

    private final Main plugin;

    public DSFriendsCommand() {
        super("armazemfriends", "Sistema de amigos", "/armazemfriends", Arrays.asList("friendsarmazem", "armazemamigos"));
        this.plugin = Main.getInstance();
    }

    public void openFriendsInventory(Player p, DropStorage dropStorage, int page) {
        final InventoryMaker inventory = new InventoryMaker(3, "§8Amigos");
        final List<String> friends = dropStorage.getFriends();
        inventory.clear();
        inventory.setCancellable(true);
        inventory.setItem(4, new Item(Material.getMaterial(397), 1, (short) 3).setSkullOwner(p.getName()).name("§e" + p.getName()).lore("§fAmigos: " + friends.size()));
        final List<Integer> slots = Arrays.asList(11, 12, 13, 14, 15);
        final double pages = Math.ceil(friends.size() / 5.0);
        if (page > pages || page <= 0) {
            p.sendMessage("§cEssa página não existe.");
            return;
        }
        if (page != pages)
            inventory.setItem(16, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eAvançar").lore("§7Clique para ir a página " + (page + 1)).setSkullUrl("http://textures.minecraft.net/texture/715445da16fab67fcd827f71bae9c1d2f90c73eb2c1bd1ef8d8396cd8e8").onClick(inventoryClickEvent -> openFriendsInventory(p, dropStorage, page + 1)));
        if (page != 1)
            inventory.setItem(10, new Item(Material.SKULL_ITEM, 1, (short) 3).name("§eVoltar").lore("§7Clique para ir a página " + (page - 1)).setSkullUrl("http://textures.minecraft.net/texture/eed78822576317b048eea92227cd85f7afcc44148dcb832733baccb8eb56fa1").onClick(inventoryClickEvent -> openFriendsInventory(p, dropStorage, page - 1)));
        final int calculate = page - 1;
        final List<String> subList = friends.subList(calculate, Math.min(calculate + 5, friends.size()));
        int count = 0;
        while (count != subList.size()) {
            if (subList.get(count) != null && Bukkit.getOfflinePlayer(subList.get(count)) != null) {
                final OfflinePlayer player = Bukkit.getOfflinePlayer(subList.get(count));
                inventory.setItem(slots.get(count),
                        new Item(Material.getMaterial(397), 1, (short) 3)
                                .name("§f" + player.getName())
                                .setSkullOwner(player.getName())
                                .lore("§fEsse jogador está " + (player.isOnline() ? "§aOnline" : "§fOffline") + "§f.", "§cClique para remover da lista de amigos.")
                                .onClick(inventoryClickEvent -> {
                                    p.closeInventory();
                                    Bukkit.dispatchCommand(p, "armazemfriends remove " + player.getName());
                                }));
                count++;
            }
        }
        inventory.open(p);
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        if (s instanceof Player) {
            final Player p = (Player) s;
            final Settings.Navigate nav = plugin.getMessages().navigate();
            final LoadingCache<String, DropStorage> cache = Caches.getCache(DropStorage.class);
            if (args.length == 0) {
                final DropStorage dropStorage = cache.getIfPresent(p.getName());
                if (dropStorage.getFriends().size() > 0) {
                    openFriendsInventory(p, dropStorage, 1);
                } else p.sendMessage(nav.getMessage("friends-0"));
            } else if (args.length == 1) {
                p.sendMessage("§aComandos Armazém - Sistema de amigos");
                p.sendMessage("");
                p.sendMessage("§a/armazemfriends - Acessa a lista de amigos.");
                p.sendMessage("§a/armazemfriends ajuda - Mostra os comandos do sistema");
                p.sendMessage("§a/armazemfriends add (player) - Adiciona um jogador a lista de amigos.");
                p.sendMessage("§a/armazemfriends remove (player) - Remove um jogador da lista de amigos.");
            } else if (args.length == 2) {
                if (Bukkit.getPlayer(args[1]) == null) {
                    p.sendMessage(nav.getMessage("player-offline"));
                    return true;
                }
                final DropStorage dropStorage = cache.getIfPresent(p.getName());
                if (args[0].equalsIgnoreCase("add")) {
                    if (dropStorage.getFriends().contains(args[1].toLowerCase())) return true;
                    if (args[1].equalsIgnoreCase(p.getName())) return true;
                    if (dropStorage.getFriends().size() == plugin.getDropStorage().navigate().getInt("Settings.friends.max")) {
                        p.sendMessage(nav.getMessage("friends-max"));
                        return true;
                    }
                    dropStorage.getFriends().add(args[1].toLowerCase());
                    p.sendMessage(nav.getMessage("friends-add").replace("{player}", p.getName()));
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (!dropStorage.getFriends().contains(args[1].toLowerCase())) return true;
                    final List<String> friends = new ArrayList<>(dropStorage.getFriends());
                    friends.remove(args[1].toLowerCase());
                    dropStorage.setFriends(friends);
                    p.sendMessage(nav.getMessage("friends-remove").replace("{player}", p.getName()));
                }
            }
        }
        return true;
    }
}