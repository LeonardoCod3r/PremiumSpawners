package centralworks.spawners.modules.animations;

import com.google.gson.annotations.Expose;
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
    @Expose
    private String locSerialized;
    @Getter
    @Setter
    @Expose
    public boolean cancelled = false;
    @Getter
    @Setter
    @Expose
    public double radius = 0.8;
    @Enumerated
    @Getter
    @Setter
    @Expose
    public AnimationType animationType;

}
