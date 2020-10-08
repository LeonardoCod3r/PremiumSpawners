package centralworks.repositories.json;

import centralworks.database.specifications.FastRepository;
import centralworks.core.commons.models.UserDetails;

public class FastUserRepository extends FastRepository<UserDetails, String> {

    private static FastUserRepository repository;

    public static FastUserRepository require() {
        return repository == null ? repository = new FastUserRepository() : repository;
    }


    public FastUserRepository() {
        super(UserDetails.class);
    }
}
