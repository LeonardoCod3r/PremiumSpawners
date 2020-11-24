package centralworks.repositories.mysql;

import centralworks.core.commons.models.User;
import centralworks.database.specifications.JpaRepository;

public class JpaUserRepository extends JpaRepository<User, String> {

    private static JpaUserRepository repository;

    public JpaUserRepository() {
        super(User.class);
    }

    public static JpaUserRepository require() {
        return repository == null ? repository = new JpaUserRepository() : repository;
    }

}
