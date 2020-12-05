package centralworks.repositories.mysql;

import centralworks.quests.models.PlayerQuests;
import centralworks.database.specifications.JpaRepository;

public class JpaUserQuestsRepository extends JpaRepository<PlayerQuests, String> {

    private static JpaUserQuestsRepository repository;

    public JpaUserQuestsRepository() {
        super(PlayerQuests.class);
    }

    public static JpaUserQuestsRepository require() {
        return repository == null ? repository = new JpaUserQuestsRepository() : repository;
    }
}
