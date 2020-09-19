package centralworks.spawners.commons.database.specifications;

public interface EntityConfiguration {

    Object getIdentifier();

    void setIdentifier(Object obj);

    <O, T> Repository<O, T> getRepository();


}
