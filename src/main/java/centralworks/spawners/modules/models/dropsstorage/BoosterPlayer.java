package centralworks.spawners.modules.models.dropsstorage;

import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.commons.database.repositories.DropStorageRepository;
import com.google.gson.annotations.Expose;
import lombok.*;

import javax.persistence.*;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class BoosterPlayer {

    @Expose(serialize = false, deserialize = false)
    @ManyToOne
    @Setter
    @Getter
    private DropStorage dropStorage;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    @Expose
    private Long id;
    @Expose
    @Getter
    @Setter
    private Double multiplier;
    @Getter
    @Setter
    @Expose
    private Integer time;

    public BoosterPlayer(DropStorage dropStorage, Double multiplier, Integer time) {
        this.dropStorage = dropStorage;
        this.multiplier = multiplier;
        this.time = time;
    }
}
