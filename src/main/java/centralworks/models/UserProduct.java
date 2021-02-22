package centralworks.models;

import centralworks.hooks.EconomyContext;
import centralworks.lib.enums.PluginSystemType;
import centralworks.market.models.Market;
import centralworks.market.models.Product;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.entity.Player;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class UserProduct {

    @Id
    @GeneratedValue
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Long id;
    @Getter(AccessLevel.PRIVATE)
    @Setter
    @ManyToOne
    @Deprecated
    private ProductStorage productStorage;
    @Getter
    @Setter
    @Expose
    private String productKey;
    @Getter
    @Setter
    @Expose
    private Double amount;

    public UserProduct(ProductStorage productStorage, String productKey, Double amount) {
        this.productStorage = productStorage;
        this.productKey = productKey;
        this.amount = amount;
    }

    public Optional<Product> getProduct() {
        return Market.getInstance().findProductById(productKey);
    }

    public void sell(Player p, ProductStorage storage) {
        getProduct().ifPresent(product -> {
            val v = product.getCurrentPrice() * amount;
            val economy = EconomyContext.getContext(PluginSystemType.PRODUCT_STORAGE).getEconomy();
            economy.addMoney(p.getName(), v + (v * storage.getBonus() / 100));
            product.sold(amount);
            setAmount(0D);
        });
    }

    public Double getPrice(ProductStorage storage) {
        val price = new AtomicReference<>(0.0);
        getProduct().ifPresent(product -> {
            val value = product.getCurrentPrice() * getAmount();
            price.set(value + (value * storage.getBonus() / 100));
        });
        return price.get();
    }

    public Double getUnitPrice(ProductStorage storage) {
        val price = getProduct().orElse(new Product()).getCurrentPrice();
        return price + (price * storage.getBonus() / 100);
    }

    public void addDropAmount(Double amount) {
        this.amount += amount;
    }

    public void removeDropAmount(Double amount) {
        this.amount -= amount;
    }
}
