package centralworks.spawners.commons.database.specifications;

import centralworks.spawners.commons.database.Storable;
import lombok.Getter;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CrudRepository<O extends Storable<O>, T> implements Repository<O, T> {

    static {
        Storage.create();
    }

    @Getter
    private final Class<O> target;

    public CrudRepository(Class<O> target) {
        this.target = target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public O commit(O obj) {
        if (exists((T) obj.getIdentifier())) return update(obj);
        final EntityManager em = Storage.getEntityManager();
        em.getTransaction().begin();
        em.persist(obj);
        em.getTransaction().commit();
        return obj;
    }

    @Override
    public Optional<O> read(T id) {
        final EntityManager em = Storage.getEntityManager();
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
        final EntityManager em = Storage.getEntityManager();
        em.getTransaction().begin();
        obj = em.merge(obj);
        em.getTransaction().commit();
        return obj;
    }

    @Override
    public void delete(O obj) {
        final EntityManager em = Storage.getEntityManager();
        em.getTransaction().begin();
        em.remove(obj);
        em.getTransaction().commit();
    }

    @Override
    public Optional<O> findAndDelete(T id) {
        final EntityManager em = Storage.getEntityManager();
        em.getTransaction().begin();
        final Optional<O> o = Optional.of(em.find(target, id));
        o.ifPresent(em::remove);
        em.getTransaction().commit();
        return o;
    }

    @Override
    public void deleteOf(T id, String idName) {
        final EntityManager em = Storage.getEntityManager();
        em.createQuery("DELETE FROM " + target.getName() + " t where w." + idName + "=:id")
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<O> findAll() {
        final EntityManager em = Storage.getEntityManager();
        return em.createQuery("FROM " + target.getName(), target).getResultList();
    }

    @Override
    public List<O> findAll(Predicate<O> predicate) {
        return findAll().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public boolean exists(T id, String idName) {
        final EntityManager em = Storage.getEntityManager();
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
        final EntityManager em = Storage.getEntityManager();
        return em.createQuery(query, target);
    }

    @Override
    public TypedQuery<O> createQuery(CriteriaQuery<O> criteriaQuery) {
        final EntityManager em = Storage.getEntityManager();
        return em.createQuery(criteriaQuery);
    }

    @Override
    public Query createNativeQuery(String query) {
        final EntityManager em = Storage.getEntityManager();
        return em.createNativeQuery(query, target);
    }
}
