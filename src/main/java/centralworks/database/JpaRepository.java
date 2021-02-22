package centralworks.database;

import com.google.inject.Inject;
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

public class JpaRepository<O, T> implements Repository<O, T> {

    @Setter
    @Getter
    @Inject
    private Session session;
    @Setter
    @Getter
    @Inject
    private EntityManager em;

    @Getter
    private final Class<O> target;

    public JpaRepository(Class<O> target) {
        this.target = target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public O commit(O obj) {
        try {
            return update(obj);
        } catch (Exception ignored) {
            em.getTransaction().begin();
            em.persist(obj);
            em.getTransaction().commit();
        }
        return obj;
    }

    @Override
    public Optional<O> read(T id) {
        em.getTransaction().begin();
        try {
            final O obj = em.find(target, id);
            return Optional.ofNullable(obj);
        } catch (Exception ignored) {
        } finally {
            em.getTransaction().commit();
        }
        return Optional.empty();
    }

    @Override
    public O update(O obj) {
        em.getTransaction().begin();
        try {
            obj = em.merge(obj);
        } catch (Exception ignored) {
        } finally {
            em.getTransaction().commit();
        }
        return obj;
    }

    @Override
    public void delete(O obj) {
        em.getTransaction().begin();
        try {
            em.remove(obj);
        } catch (Exception ignored) {
        } finally {
            em.getTransaction().commit();
        }
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
    public List<O> findAll() {
        return em.createQuery("FROM " + target.getName(), target).getResultList();
    }

    @Override
    public List<O> findAll(Predicate<O> predicate) {
        return findAll().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public boolean exists(T id) {
        em.getTransaction().begin();
        boolean result = false;
        try {
            final Optional<O> o = Optional.ofNullable(em.find(target, id));
            result = o.isPresent();
        } catch (Exception ignored) {
        }
        em.getTransaction().commit();
        return result;
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
