package centralworks.market.models;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class Market {

    static {
        instance = new Market();
    }

    @Getter
    private static Market instance;

    @Setter
    @Getter
    public List<Product> products;

    public Optional<Product> findProductById(String id) {
        return products.stream().filter(product -> product.getId().equalsIgnoreCase(id)).findFirst();
    }


}
