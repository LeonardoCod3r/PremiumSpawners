package centralworks.commands;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.lib.ActionBarMessage;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.Settings;
import centralworks.lib.enums.ItemName;
import centralworks.lib.enums.Permission;
import centralworks.lib.inventory.InventoryMaker;
import centralworks.lib.inventory.Item;
import centralworks.market.models.Product;
import centralworks.models.ProductStorage;
import centralworks.models.UserProduct;
import com.google.common.cache.LoadingCache;
import lombok.val;
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
            final LoadingCache<String, ProductStorage> cache = Caches.getCache(ProductStorage.class);
            if (args.length == 0) {
                final ProductStorage productStorage = cache.getIfPresent(p.getName());
                openSellInventory(p, productStorage, nav);
            } else if (args.length == 1) {
                final Optional<ProductStorage> optional = Optional.ofNullable(cache.getIfPresent(args[0]));
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

    public void openSellInventory(Player p, ProductStorage productStorage, Settings.Navigate nav) {
        final InventoryMaker inventory = new InventoryMaker(nav.getInt("Inventory.sell.rows"), nav.getColorfulString("Inventory.sell.name"));
        final List<Integer> slots = new ArrayList<>();
        final Settings.Navigate messages = plugin.getMessages().navigate();
        Arrays.asList(nav.getString("Inventory.sell.slotsDropSell").split(",")).forEach(s1 -> slots.add(Integer.parseInt(s1)));
        inventory.setCancellable(true);
        inventory.clear();
        String path = "Inventory.sell.items.playerStats.";
        if (nav.getBoolean(path + "toggle")) {
            inventory.setItem(nav.getInt(path + "slot"),
                    new Item(Material.getMaterial(nav.getString(path + "id")), 1, nav.getInt(path + "data").shortValue())
                            .name(nav.getColorfulString(path + "name").replace("{playername}", productStorage.getCorrectNameOwner()))
                            .setSkullUrl(nav.getString(path + "skull-url"))
                            .setSkullOwner(nav.getColorfulString(path + "skull-owner").replace("{playername}", productStorage.getCorrectNameOwner()))
                            .lore(nav.getColorfulList(path + "lore").stream().map(s1 -> s1
                                    .replace("{limit}", BalanceFormatter.format(productStorage.getUser().getSellLimit()))
                                    .replace("{bonus}", productStorage.getBonus().toString())
                                    .replace("{multiplier}", productStorage.getMultiplier().toString()))
                                    .collect(Collectors.toList())));
        }
        path = "Inventory.sell.items.sellAll.";
        if (nav.getBoolean(path + "toggle")) {
            inventory.setItem(nav.getInt(path + "slot"),
                    new Item(Material.getMaterial(nav.getString(path + "id")), 1, nav.getInt(path + "data").shortValue())
                            .name(nav.getColorfulString(path + "name").replace("{playername}", productStorage.getCorrectNameOwner()))
                            .setSkullUrl(nav.getString(path + "skull-url"))
                            .setSkullOwner(nav.getColorfulString(path + "skull-owner").replace("{playername}", productStorage.getCorrectNameOwner()))
                            .lore(nav.getColorfulList(path + "lore").stream().map(s1 -> s1
                                    .replace("{drops}", BalanceFormatter.format(productStorage.getAmountAll()))
                                    .replace("{total}", BalanceFormatter.format(productStorage.getPriceAll())))
                                    .collect(Collectors.toList()))
                            .onClick(inventoryClickEvent -> {
                                if (!Permission.hasPermission(p, Permission.SELL_ALL)) return;
                                if (productStorage.getAmountAll() > 0) {
                                    new ActionBarMessage(p, messages.getMessage("drops-sellall")
                                            .replace("{amount}", BalanceFormatter.format(productStorage.getAmountAll()))
                                            .replace("{price}", BalanceFormatter.format(productStorage.getPriceAll())));
                                    productStorage.sellAll();
                                    Bukkit.dispatchCommand(p, "drops " + productStorage.getOwner());
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
                                    .replace("{state}", productStorage.isAutoSellResult())
                                    .replace("{future-state}", "" + (!productStorage.isAutoSell() ? "ativar" : "desativar")))
                                    .collect(Collectors.toList()))
                            .onClick(inventoryClickEvent -> {
                                if (!Permission.hasPermission(p, Permission.AUTO_SELL))
                                    productStorage.setAutoSell(false);
                                else productStorage.setAutoSell(!productStorage.isAutoSell());
                                Bukkit.dispatchCommand(p, "drops " + productStorage.getOwner());
                            }));
        }
        final List<UserProduct> dropsPlayer = productStorage.getUserProducts().stream().filter(userProduct -> userProduct.getAmount() > 0).collect(Collectors.toList());
        if (dropsPlayer.size() != 0) {
            for (int count = 0; count < slots.size(); count++) {
                if (dropsPlayer.size() == count) break;
                val userProduct = dropsPlayer.get(count);
                val product = userProduct.getProduct().orElse(new Product());
                inventory.setItem(slots.get(count),
                        product.getItemMenu(s -> s
                                .replace("{price-sell-unit}", BalanceFormatter.format(userProduct.getUnitPrice(productStorage)))
                                .replace("{amount}", BalanceFormatter.format(userProduct.getAmount()))
                                .replace("{price-sell-all}", BalanceFormatter.format(userProduct.getPrice(productStorage))))
                                .onClick(event -> {
                                    if (userProduct.getAmount() > 0) {
                                        new ActionBarMessage(p, messages.getMessage("drops-sell")
                                                .replace("{amount}", BalanceFormatter.format(userProduct.getAmount()))
                                                .replace("{item}", ItemName.valueOf(product.getId() + "__" + product.getData()).getName())
                                                .replace("{price}", BalanceFormatter.format(userProduct.getPrice(productStorage))));
                                        userProduct.sell(p, productStorage);
                                    }
                                    Bukkit.dispatchCommand(p, "drops " + productStorage.getOwner());
                                }));
            }
        }
        inventory.open(p);
    }
}
