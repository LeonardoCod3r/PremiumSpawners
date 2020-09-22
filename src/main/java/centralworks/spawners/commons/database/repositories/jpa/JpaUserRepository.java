package centralworks.spawners.commons.database.repositories.jpa;

import centralworks.spawners.commons.database.specifications.JpaRepository;
import centralworks.spawners.modules.models.UserDetails;

public class JpaUserRepository extends JpaRepository<UserDetails, String> {
    
    private static JpaUserRepository repository;

    public static JpaUserRepository require() {
        return repository == null ? repository = new JpaUserRepository() : repository;
    }

    public JpaUserRepository() {
        super(UserDetails.class);
    }

}
