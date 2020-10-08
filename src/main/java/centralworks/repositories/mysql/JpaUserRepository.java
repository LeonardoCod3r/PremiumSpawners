package centralworks.repositories.mysql;

import centralworks.database.specifications.JpaRepository;
import centralworks.core.commons.models.UserDetails;

public class JpaUserRepository extends JpaRepository<UserDetails, String> {
    
    private static JpaUserRepository repository;

    public static JpaUserRepository require() {
        return repository == null ? repository = new JpaUserRepository() : repository;
    }

    public JpaUserRepository() {
        super(UserDetails.class);
    }

}
