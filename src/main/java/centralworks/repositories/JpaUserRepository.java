package centralworks.repositories;

import centralworks.Main;
import centralworks.database.JpaRepository;
import centralworks.models.User;

public class JpaUserRepository extends JpaRepository<User, String> {

    private static JpaUserRepository repository;

    public JpaUserRepository() {
        super(User.class);
    }

    public static JpaUserRepository require() {
        return repository == null ? repository = Main.getInstance().getInjector().getInstance(JpaUserRepository.class) : repository;
    }

}
