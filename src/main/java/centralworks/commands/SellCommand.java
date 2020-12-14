package centralworks.commands;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.cache.simple.LootData;
import centralworks.models.Drop;
import centralworks.models.DropPlayer;
import centralworks.models.DropStorage;
import centralworks.lib.ActionBarMessage;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Settings;
import centralworks.lib.enums.ItemName;
import centralworks.lib.enums.Permission;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SellCommand extends BukkitCommand {

    private final Main plugin;

    public SellCommand() {
        super("armazem", "Comando de vender drops", "/dropStorage", Arrays.asList("vender", "drops", "sell"));
        this.plugin = Main.getInstance();
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        if (s instanceof Player) {
            final Player p = (Player) s;
            final Settings.Navigate nav = plugin.getDropStorage().navigate();
            final LoadingCache<String, DropStorage> cache = Caches.getCache(DropStorage.class);
            if (args.length == 0) {
                final DropStorage dropStorage = cache.getIfPresent(p.getName());
                openSellInventory(p, dropStorage, nav);
            } else if (args.length == 1) {
                final Optional<DropStorage> optional = Optional.ofNullable(cache.getIfPresent(args[0]));
                final boolean present = optional.isPresent();
                optional.ifPresent(dropStorage -> {
                    if (dropStorage.getFriends().stream().anyMatch(s1 -> s1.equalsIgnoreCase(p.getName())) || args[0].equalsIgnoreCase(p.getName())) {
                        openSellInventory(p, dropStorage, nav);
                    }
                });
                if (!present) p.sendMessage(plugin.getMessages().navigate().getMessage("drops-no-friend"));
            }
        }
        return true;
    }

    public void openSellInventory(Player p, DropStorage dropStorage, Settings.Navigate nav) {
        final InventoryMaker inventory = new InventoryMaker(nav.getInt("Inventory.sell.rows"), nav.getColorfulString("Inventory.sell.name"));
        final List<Integer> slots = new ArrayList<>();
        final LootData cached = LootData.get();
        final Settings.Navigate messages = plugin.getMessages().navigate();
        final LoadingCache<String, DropStorage> cache = Caches.getCache(DropStorage.class);
        Arrays.asList(nav.getString("Inventory.sell.slotsDropSell").split(",")).forEach(s1 -> slots.add(Integer.parseInt(s1)));
        inventory.setCancellable(true);
        inventory.clear();
        String path = "Inventory.sell.items.playerStats.";
        if (nav.getBoolean(path + "toggle")) {
            inventory.setItem(nav.getInt(path + "slot"),
                    new Item(Material.getMaterial(nav.getString(path + "id")), 1, nav.getInt(path + "data").shortValue())
                            .name(nav.getColorfulString(path + "name").replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .setSkullUrl(nav.getString(path + "skull-url"))
                            .setSkullOwner(nav.getColorfulString(path + "skull-owner").replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .lore(nav.getColorfulList(path + "lore").stream().map(s1 -> s1
                                    .replace("{limit}", BalanceFormatter.format(dropStorage.getUser().getSellLimit()))
                                    .replace("{bonus}", dropStorage.getBonus().toString())
                                    .replace("{multiplier}", dropStorage.getMultiplier().toString()))
                                    .collect(Collectors.toList())));
        }
        path = "Inventory.sell.items.sellAll.";
        if (nav.getBoolean(path + "toggle")) {
            inventory.setItem(nav.getInt(path + "slot"),
                    new Item(Material.getMaterial(nav.getString(path + "id")), 1, nav.getInt(path + "data").shortValue())
                            .name(nav.getColorfulString(path + "name").replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .setSkullUrl(nav.getString(path + "skull-url"))
                            .setSkullOwner(nav.getColorfulString(path + "skull-owner").replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .lore(nav.getColorfulList(path + "lore").stream().map(s1 -> s1
                                    .replace("{drops}", BalanceFormatter.format(dropStorage.getAmountAll()))
                                    .replace("{total}", BalanceFormatter.format(dropStorage.getPriceWithBonus())))
                                    .collect(Collectors.toList()))
                            .onClick(inventoryClickEvent -> {
                                if (!Permission.hasPermission(p, Permission.SELL_ALL)) return;
                                final DropStorage storage = cache.getIfPresent(dropStorage.getOwner());
                                if (storage.getAmountAll() > 0) {
                                    new ActionBarMessage(p, messages.getMessage("drops-sellall")
                                            .replace("{amount}", BalanceFormatter.format(dropStorage.getAmountAll()))
                                            .replace("{price}", BalanceFormatter.format(storage.getPriceWithBonus())));
                                    storage.sellAll();
                                    Bukkit.dispatchCommand(p, "drops " + dropStorage.getOwner());
                                }
                            }));
        }
        path = "Inventory.sell.items.autoSell.";
        if (nav.getBoolean(path + "toggle")) {
            inventory.setItem(nav.getInt(path + "slot"),
                    new Item(Material.getMaterial(nav.getString(path + "id")), 1, nav.getInt(path + "data").shortValue())
                            .name(nav.getColorfulString(path + "name").replace("{playername}", p.getName()))
                            .setSkullUrl(nav.getString(path + "skull-url"))
                            .setSkullOwner(nav.getColorfulString(path + "skull-owner").replace("{playername}", p.getName()))
                            .lore(nav.getColorfulList(path + "lore").stream().map(s1 -> s1
                                    .replace("{state}", dropStorage.isAutoSellResult())
                                    .replace("{future-state}", "" + (!dropStorage.isAutoSell() ? "ativar" : "desativar")))
                                    .collect(Collectors.toList()))
                            .onClick(inventoryClickEvent -> {
                                final DropStorage storage = cache.getIfPresent(dropStorage.getOwner());
                                if (!Permission.hasPermission(p, Permission.AUTO_SELL)) storage.setAutoSell(false);
                                else storage.setAutoSell(!storage.isAutoSell());
                                Bukkit.dispatchCommand(p, "drops " + storage.getOwner());
                            }));
        }
        final List<DropPlayer> dropsPlayer = dropStorage.getDropPlayers().stream().filter(dropPlayer -> dropPlayer.getAmount() > 0).collect(Collectors.toList());
        if (dropsPlayer.size() != 0) {
            for (int count = 0; count < slots.size(); count++) {
                if (dropsPlayer.size() == count) break;
                final DropPlayer dropPlayer = dropsPlayer.get(count);
                final Drop drop = cached.get(dropPlayer.getKeyDrop());
                final List<String> lore = drop.getMenuItem().getItemMeta().getLore();
                inventory.setItem(slots.get(count),
                        new Item(drop.getMenuItem().getItemStack().clone())
                                .lore(lore.stream().map(s1 -> s1
                                        .replace("{price-sell-unit}", BalanceFormatter.format(drop.getUnitPrice() + (drop.getUnitPrice() * dropStorage.getBonus() / 100)))
                                        .replace("{amount}", BalanceFormatter.format(dropPlayer.getAmount()))
                                        .replace("{price-sell-all}", BalanceFormatter.format(dropPlayer.getPrice(dropStorage))))
                                        .collect(Collectors.toList()))
                                .onClick(event -> {
                                    final DropStorage storage = cache.getIfPresent(dropStorage.getOwner());
                                    final DropPlayer dp = storage.getDropPlayer(dropPlayer.getKeyDrop());
                                    if (dp.getAmount() > 0) {
                                        new ActionBarMessage(p, messages.getMessage("drops-sell")
                                                .replace("{amount}", BalanceFormatter.format(dp.getAmount()))
                                                .replace("{item}", ItemName.valueOf(drop.getDrop()).getName())
                                                .replace("{price}", BalanceFormatter.format(dp.getPrice(storage))));
                                        dp.sell(p, storage);
                                    }
                                    Bukkit.dispatchCommand(p, "drops " + dropStorage.getOwner());
                                }));
            }
        }
        inventory.open(p);
    }
}
