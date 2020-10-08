package centralworks.database.specifications;

import centralworks.Main;
import centralworks.database.Storable;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JpaRepository<O extends Storable<O>, T> implements Repository<O, T> {

    static {
        final Injector injector = Main.getInstance().getInjector();;
        session = injector.getInstance(Session.class);
        em = injector.getInstance(EntityManager.class);
    }

    @Setter
    @Getter
    private static Session session;
    @Setter
    @Getter
    private static EntityManager em;

    @Getter
    private final Class<O> target;

    public JpaRepository(Class<O> target) {
        this.target = target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public O commit(O obj) {
        if (exists((T) obj.getEntityIdentifier())) return update(obj);
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        return obj;
    }

    @Override
    public Optional<O> read(T id) {
        em.getTransaction().begin();
        try {
            final O obj = em.find(target, id);
            em.getTransaction().commit();
            return Optional.ofNullable(obj);
        }catch (Exception ignored){
            if (em.getTransaction().isActive()) em.getTransaction().commit();
        }
        return Optional.empty();
    }

    @Override
    public O update(O obj) {
        em.getTransaction().begin();
        obj = em.merge(obj);
        em.getTransaction().commit();
        return obj;
    }

    @Override
    public void delete(O obj) {
        em.getTransaction().begin();
        em.remove(obj);
        em.getTransaction().commit();
    }

    @Override
    public Optional<O> findAndDelete(T id) {
        em.getTransaction().begin();
        final Optional<O> o = Optional.of(em.find(target, id));
        o.ifPresent(em::remove);
        em.getTransaction().commit();
        return o;
    }

    @Override
    public void deleteOf(T id, String idName) {
        em.createQuery("DELETE FROM " + target.getName() + " t where w." + idName + "=:id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<O> findAll() {
        return em.createQuery("FROM " + target.getName(), target).getResultList();
    }

    @Override
    public List<O> findAll(Predicate<O> predicate) {
        return findAll().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public boolean exists(T id, String idName) {
        final Long result = ((Long) em.createQuery("SELECT count(o) from " + target.getName() + " o where w." + idName + "=:id")
                .setParameter("id", id)
                .getSingleResult());
        return !result.equals(0L);
    }

    @Override
    public boolean exists(T id) {
        try {
            return read(id).isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public TypedQuery<O> createQuery(String query) {
        return em.createQuery(query, target);
    }

    @Override
    public TypedQuery<O> createQuery(CriteriaQuery<O> criteriaQuery) {
        return em.createQuery(criteriaQuery);
    }

    @Override
    public Query createNativeQuery(String query) {
        return em.createNativeQuery(query, target);
    }
}
