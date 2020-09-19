package centralworks.spawners.modules.models.dropsstorage;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@AllArgsConstructor
@RequiredArgsConstructor
@Entity
public class BoosterPlayer {

    @Id
    @Column(length = 16)
    @Getter
    @Setter
    private String owner;
    @Getter
    @Setter
    private Double multiplier;
    @Getter
    @Setter
    private Integer time;

}
