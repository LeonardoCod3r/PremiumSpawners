package centralworks.spawners.modules.cmds;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.lib.*;
import centralworks.spawners.modules.models.ActionBarMessage;
import centralworks.spawners.modules.models.dropsstorage.DropPlayer;
import centralworks.spawners.modules.models.dropsstorage.DropStorage;
import centralworks.spawners.modules.models.dropsstorage.supliers.Drop;
import centralworks.spawners.modules.models.dropsstorage.supliers.cached.LootData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SellCommand extends BukkitCommand {

    public SellCommand() {
        super("armazem", "Comando de vender drops", "/dropStorage", Arrays.asList("vender", "drops", "sell"));
    }

    @Override
    public boolean execute(CommandSender s, String lbl, String[] args) {
        if (s instanceof Player) {
            final Player p = (Player) s;
            final Configuration configuration = Main.getDropStorage();
            if (args.length == 0) {
                final DropStorage dropStorage = new DropStorage(p).query().persist();
                openSellInventory(p, dropStorage, configuration);
            } else if (args.length == 1) {
                final SyncRequests<DropStorage, Object> q = new DropStorage(args[0]).query();
                q.ifExists(dropStorage -> {
                    if (dropStorage.getFriends().stream().anyMatch(s1 -> s1.equalsIgnoreCase(p.getName())) || args[0].equalsIgnoreCase(p.getName())) {
                        openSellInventory(p, dropStorage, configuration);
                    }
                }, exception -> p.sendMessage(Main.getMessages().getMessage("drops-no-friend")));
            }
        }
        return true;
    }

    public void openSellInventory(Player p, DropStorage dropStorage, Configuration configuration) {
        final InventoryBuilder inventoryBuilder = new InventoryBuilder(Main.get(), configuration.getInt("Inventory.sell.rows"), configuration.get("Inventory.sell.name", true));
        final List<Integer> slots = new ArrayList<>();
        final LootData cached = LootData.get();
        final Configuration messages = Main.getMessages();
        Arrays.asList(configuration.get("Inventory.sell.slotsDropSell", true).split(",")).forEach(s1 -> slots.add(Integer.parseInt(s1)));
        inventoryBuilder.setCancellable(true);
        inventoryBuilder.clear();
        String path = "Inventory.sell.items.playerStats.";
        if (configuration.is(path + "toggle")) {
            inventoryBuilder.setItem(configuration.getInt(path + "slot"),
                    new Item(Material.getMaterial(configuration.getInt(path + "id")), 1, configuration.getInt(path + "data").shortValue())
                            .name(configuration.get(path + "name", true).replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .setSkullUrl(configuration.get(path + "skull-url", false))
                            .setSkullOwner(configuration.get(path + "skull-owner", true).replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .lore(configuration.getList(path + "lore", true).stream().map(s1 -> s1
                                    .replace("{limit}", FormatBalance.format(dropStorage.getUser().getSellLimit()))
                                    .replace("{bonus}", dropStorage.getBonus().toString())
                                    .replace("{multiplier}", dropStorage.getMultiplier().toString()))
                                    .collect(Collectors.toList())));
        }
        path = "Inventory.sell.items.sellAll.";
        if (configuration.is(path + "toggle")) {
            inventoryBuilder.setItem(configuration.getInt(path + "slot"),
                    new Item(Material.getMaterial(configuration.getInt(path + "id")), 1, configuration.getInt(path + "data").shortValue())
                            .name(configuration.get(path + "name", true).replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .setSkullUrl(configuration.get(path + "skull-url", false))
                            .setSkullOwner(configuration.get(path + "skull-owner", true).replace("{playername}", dropStorage.getCorrectNameOwner()))
                            .lore(configuration.getList(path + "lore", true).stream().map(s1 -> s1
                                    .replace("{drops}", FormatBalance.format(dropStorage.getAmountAll()))
                                    .replace("{total}", FormatBalance.format(dropStorage.getPriceWithBonus())))
                                    .collect(Collectors.toList()))
                            .onClick(inventoryClickEvent -> {
                                if (!Permission.hasPermission(p, Permission.SELL_ALL)) return;
                                dropStorage.query().queue(storage -> {
                                    if (storage.getAmountAll() > 0) {
                                        new ActionBarMessage(p, messages.getMessage("drops-sellall")
                                                .replace("{amount}", FormatBalance.format(dropStorage.getAmountAll()))
                                                .replace("{price}", FormatBalance.format(storage.getPriceWithBonus())));
                                        storage.sellAll();
                                        Bukkit.dispatchCommand(p, "drops " + dropStorage.getOwner());
                                    }
                                });
                            }));
        }
        path = "Inventory.sell.items.autoSell.";
        if (configuration.is(path + "toggle")) {
            inventoryBuilder.setItem(configuration.getInt(path + "slot"),
                    new Item(Material.getMaterial(configuration.getInt(path + "id")), 1, configuration.getInt(path + "data").shortValue())
                            .name(configuration.get(path + "name", true).replace("{playername}", p.getName()))
                            .setSkullUrl(configuration.get(path + "skull-url", false))
                            .setSkullOwner(configuration.get(path + "skull-owner", true).replace("{playername}", p.getName()))
                            .lore(configuration.getList(path + "lore", true).stream().map(s1 -> s1
                                    .replace("{state}", dropStorage.isAutoSellResult())
                                    .replace("{future-state}", "" + (!dropStorage.isAutoSell() ? "ativar" : "desativar")))
                                    .collect(Collectors.toList()))
                            .onClick(inventoryClickEvent -> dropStorage.query().queue((storage, q) -> {
                                if (!Permission.hasPermission(p, Permission.AUTO_SELL)) {
                                    storage.setAutoSell(false);
                                } else storage.setAutoSell(!storage.isAutoSell());
                                q.commit();
                                Bukkit.dispatchCommand(p, "drops " + storage.getOwner());
                            })));
        }
        final List<DropPlayer> dropsPlayer = dropStorage.getDropPlayers().stream().filter(dropPlayer -> dropPlayer.getAmount() > 0).collect(Collectors.toList());
        if (dropsPlayer.size() != 0) {
            for (int count = 0; count < slots.size(); count++) {
                if (dropsPlayer.size() == count) break;
                final DropPlayer dropPlayer = dropsPlayer.get(count);
                final Drop drop = cached.get(dropPlayer.getKeyDrop());
                final List<String> lore = drop.getMenuItem().getItemMeta().getLore();
                inventoryBuilder.setItem(slots.get(count),
                        new Item(drop.getMenuItem().build().clone())
                                .lore(lore.stream().map(s1 -> s1
                                        .replace("{price-sell-unit}", FormatBalance.format(drop.getUnitPrice() + (drop.getUnitPrice() * dropStorage.getBonus() / 100)))
                                        .replace("{amount}", FormatBalance.format(dropPlayer.getAmount()))
                                        .replace("{price-sell-all}", FormatBalance.format(dropPlayer.getPrice(dropStorage))))
                                        .collect(Collectors.toList()))
                                .onClick(inventoryClickEvent -> dropStorage.query().queue((storage, q) -> {
                                    final DropPlayer dp = storage.getDropPlayer(dropPlayer.getKeyDrop());
                                    if (dp.getAmount() > 0) {
                                        new ActionBarMessage(p, messages.getMessage("drops-sell")
                                                .replace("{amount}", FormatBalance.format(dp.getAmount()))
                                                .replace("{item}", ItemName.valueOf(drop.getDrop()).getName())
                                                .replace("{price}", FormatBalance.format(dp.getPrice(storage))));
                                        dp.sell(p, storage);
                                    }
                                    q.commit();
                                    Bukkit.dispatchCommand(p, "drops " + dropStorage.getOwner());
                                })));
            }
        }
        inventoryBuilder.open(p);
    }
}
