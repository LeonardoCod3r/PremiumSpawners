package centralworks.market.models;

import centralworks.Main;
import centralworks.lib.ItemSettings;
import centralworks.lib.enums.EntityName;
import centralworks.lib.inventory.Item;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private short data;
    private String mob;
    private ItemSettings lootItemMenu;
    private RulesMarket rulesMarket;
    private Double soldAmount = 0.0;
    private Double currentPrice = 0.0;
    @Getter(AccessLevel.PRIVATE)
    private int holdKey;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private int idTask;

    public void resetSoldAmount() {
        this.soldAmount = 0.0;
    }

    public void runDelayToResetSoldAmount() {
        idTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), this::resetSoldAmount, 60 * 60 * 20L).getTaskId();
    }

    public EntityType getEntityType() {
        return EntityType.valueOf(mob);
    }

    public String getEntityName() {
        return EntityName.valueOf(mob).getName();
    }

    public Item getItemMenu(Function<String, String> loreReplacement) {
        return new Item(lootItemMenu.getAsItem(loreReplacement));
    }

    public void calculatePrice(int holdKey) {
        if (holdKey == -1 || this.holdKey == holdKey) {
            var valor = 0.0;
            if (rulesMarket.realistic) valor = rulesMarket.originalPrice;
            else {
                var current = rulesMarket.originalPrice;
                var ratio = rulesMarket.amountMax / soldAmount;
                current += ratio * rulesMarket.bonus - rulesMarket.bonus;
                current = Math.ceil(current);
                current = ratio < 1 ? Math.max(rulesMarket.priceMin, current) : Math.min(rulesMarket.priceMax, current);
                valor = current;
            }
            setCurrentPrice(valor);
        }
    }

    public void addSoldAmount(Double soldAmount) {
        this.soldAmount += soldAmount;
    }

    public void removeSoldAmount(Double soldAmount) {
        this.soldAmount -= soldAmount;
    }

    public void sold(Double amount) {
        val holdKey = ThreadLocalRandom.current().nextInt(10000);
        this.setHoldKey(holdKey);
        this.addSoldAmount(amount);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> this.calculatePrice(holdKey));
    }

    public static void sold(String idProduct, Double amount) {
        val market = Market.getInstance();
        market.findProductById(idProduct).ifPresent(product -> product.sold(amount));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RulesMarket {

        private boolean realistic = false;
        private Double priceMin;
        private Double priceMax;
        private Double originalPrice;
        private Double bonus;
        private Double amountMax;

    }

}
