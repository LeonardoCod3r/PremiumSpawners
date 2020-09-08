package centralworks.spawners.modules.models.dropsstorage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Data
public class BoosterPlayer {

    private Double multiplier;
    private Integer time;

}
