package centralworks.spawners.commons.database.repositories.fast;

import centralworks.spawners.commons.database.specifications.FastRepository;
import centralworks.spawners.modules.models.UserDetails;

public class FastUserRepository extends FastRepository<UserDetails, String> {

    private static FastUserRepository repository;

    public static FastUserRepository require() {
        return repository == null ? repository = new FastUserRepository() : repository;
    }


    public FastUserRepository() {
        super(UserDetails.class);
    }
}
