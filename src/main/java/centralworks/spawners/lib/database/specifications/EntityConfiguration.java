package centralworks.spawners.lib.database.specifications;

public interface EntityConfiguration {

    Object getEntityIdentifier();

    <O, T> Repository<O, T> getRepository();


}
