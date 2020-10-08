package centralworks.repositories.mysql;

import centralworks.database.specifications.JpaRepository;
import centralworks.core.quests.models.PlayerQuests;

public class JpaUserQuestsRepository extends JpaRepository<PlayerQuests, String> {

    private static JpaUserQuestsRepository repository;

    public static JpaUserQuestsRepository require() {
        return repository == null ? repository = new JpaUserQuestsRepository() : repository;
    }

    public JpaUserQuestsRepository() {
        super(PlayerQuests.class);
    }
}
