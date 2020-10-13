package centralworks.repositories.mysql;

import centralworks.core.commons.models.UserDetails;
import centralworks.database.specifications.JpaRepository;

public class JpaUserRepository extends JpaRepository<UserDetails, String> {

    private static JpaUserRepository repository;

    public JpaUserRepository() {
        super(UserDetails.class);
    }

    public static JpaUserRepository require() {
        return repository == null ? repository = new JpaUserRepository() : repository;
    }

}
