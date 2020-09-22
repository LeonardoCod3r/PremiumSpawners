package centralworks.spawners.test;

import centralworks.spawners.commons.database.specifications.Repository;
import centralworks.spawners.commons.database.Storable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class Testing extends Storable<Testing> implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String nickname;
    private Integer years;
    @ElementCollection
    @JoinColumn(name = "id", nullable = false)
    private List<String> lista= Lists.newArrayList();

    @Override
    public Object getEntityIdentifier() {
        return null;
    }

    @Override
    public <O, T> Repository<O, T> getRepository() {
        return null;
    }
}
