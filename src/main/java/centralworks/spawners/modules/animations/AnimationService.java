package centralworks.spawners.modules.animations;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@RequiredArgsConstructor
@AllArgsConstructor
@Entity(name = "animations")
public class AnimationService {

    @Id
    @Column(length = 150)
    @Getter
    @Setter
    private String locSerialized;
    @Getter
    @Setter
    public boolean cancelled = false;
    @Getter
    @Setter
    public double radius = 0.8;
    @Enumerated
    @Getter
    @Setter
    public AnimationType animationType;

}
