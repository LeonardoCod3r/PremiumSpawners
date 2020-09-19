package centralworks.spawners.commons.database.repositories;

import centralworks.spawners.commons.database.specifications.CrudRepository;
import centralworks.spawners.modules.models.UserDetails;

public class UserRepository extends CrudRepository<UserDetails, String> {
    
    private static UserRepository repository;

    public static UserRepository require() {
        return repository == null ? repository = new UserRepository() : repository;
    }

    public UserRepository() {
        super(UserDetails.class);
    }

}
