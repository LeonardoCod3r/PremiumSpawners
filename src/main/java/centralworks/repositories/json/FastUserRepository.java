package centralworks.repositories.json;

import centralworks.models.User;
import centralworks.database.specifications.FastRepository;

public class FastUserRepository extends FastRepository<User, String> {

    private static FastUserRepository repository;

    public FastUserRepository() {
        super(User.class);
    }

    public static FastUserRepository require() {
        return repository == null ? repository = new FastUserRepository() : repository;
    }
}
