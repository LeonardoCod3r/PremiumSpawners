package centralworks.spawners.commons.database.specifications;

public interface EntityConfiguration {

    Object getEntityIdentifier();

    <O, T> Repository<O, T> getRepository();


}
