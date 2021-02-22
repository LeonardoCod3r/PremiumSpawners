package centralworks.market.models;

import centralworks.Main;
import centralworks.lib.ItemSettings;
import centralworks.lib.enums.EntityName;
import centralworks.lib.inventory.Item;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

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

    public static void sold(String idProduct, Double amount) {
        val market = Market.getInstance();
        market.findProductById(idProduct).ifPresent(product -> product.sold(amount));
    }

    public void resetSoldAmount() {
        this.soldAmount = 0.0;
    }

    public void runDelayToResetSoldAmount() {
        idTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this::resetSoldAmount, 60 * 60 * 20L, 60 * 60 * 20L).getTaskId();
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
        var valor = 0.0;
        if (!rulesMarket.realistic || rulesMarket.amountMax < this.soldAmount) valor = rulesMarket.originalPrice;
        else {
            var current = rulesMarket.originalPrice;
            var ratio = (this.soldAmount - rulesMarket.amountMax) / rulesMarket.baseForCalculateFall;
            current -= ratio * rulesMarket.repercussionForFall / 100;
            current = Math.max(rulesMarket.priceMin, current);
            valor = current;
        }
        if (holdKey == -1 || this.holdKey == holdKey) {
            setCurrentPrice(valor);
        }
    }

    public void addSoldAmount(Double soldAmount) {
        this.soldAmount += soldAmount;
    }

    public void removeSoldAmount(Double soldAmount) {
        this.soldAmount -= soldAmount;
    }

    public void firstValue() {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> this.calculatePrice(-1));
    }

    public void sold(Double amount) {
        val holdKey = ThreadLocalRandom.current().nextInt(10000);
        this.setHoldKey(holdKey);
        this.addSoldAmount(amount);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> this.calculatePrice(holdKey));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RulesMarket {

        private boolean realistic = false;
        private Double priceMin;
        private Double baseForCalculateFall;
        private Double originalPrice;
        private Double repercussionForFall;
        private Double amountMax;

    }

}
