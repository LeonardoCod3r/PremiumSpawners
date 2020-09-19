package centralworks.spawners.commons.database.specifications;

import centralworks.spawners.commons.database.Storable;

public class QueryFunctions {

    public static <O extends Storable<O>,T> void saveAll(Repository<O, T> repository){
        new DTO<>(repository.getTarget()).findAllFiles().forEach(repository::commit);
    }
    
    public static <O extends Storable<O>, T> void loadAll(Repository<O, T> repository) {
        repository.findAll().forEach(o -> o.query().commit());
    }

}
