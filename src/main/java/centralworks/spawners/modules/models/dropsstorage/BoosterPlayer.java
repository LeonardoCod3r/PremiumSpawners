package centralworks.spawners.modules.models.dropsstorage;

import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class BoosterPlayer {

    @Getter
    @Setter
    @ManyToOne
    @Expose(serialize = false, deserialize = false)
    private DropStorage dropStorage;
    @Getter
    @Setter
    private Double multiplier;
    @Getter
    @Setter
    private Integer time;

}
