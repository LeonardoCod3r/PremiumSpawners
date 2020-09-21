package centralworks.spawners.commons.database.specifications;

public interface EntityConfiguration {

    Object getEntityIdentifier();

    void setEntityIdentifier(Object obj);

    <O, T> Repository<O, T> getRepository();


}
