package centralworks.database;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<O, T> {

    O commit(O obj);

    Optional<O> read(T id);

    O update(O obj);

    void delete(O obj);

    Optional<O> findAndDelete(T id);

    List<O> findAll();

    List<O> findAll(Predicate<O> predicate);

    boolean exists(T id);

    Query createQuery(String query);

    Query createNativeQuery(String query);

    Query createQuery(CriteriaQuery<O> criteriaQuery);

    Class<O> getTarget();

}
