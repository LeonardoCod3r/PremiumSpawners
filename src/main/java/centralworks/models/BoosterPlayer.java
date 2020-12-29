package centralworks.models;

import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class BoosterPlayer {

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
    private Double multiplier;
    @Getter
    @Setter
    @Expose
    private Integer time;

    public BoosterPlayer(ProductStorage productStorage, Double multiplier, Integer time) {
        this.productStorage = productStorage;
        this.multiplier = multiplier;
        this.time = time;
    }
}
