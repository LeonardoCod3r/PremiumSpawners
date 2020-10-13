package centralworks.repositories.json;

import centralworks.core.commons.models.UserDetails;
import centralworks.database.specifications.FastRepository;

public class FastUserRepository extends FastRepository<UserDetails, String> {

    private static FastUserRepository repository;

    public FastUserRepository() {
        super(UserDetails.class);
    }

    public static FastUserRepository require() {
        return repository == null ? repository = new FastUserRepository() : repository;
    }
}
