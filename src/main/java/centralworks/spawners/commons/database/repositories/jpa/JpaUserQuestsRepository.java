package centralworks.spawners.commons.database.repositories.jpa;

import centralworks.spawners.commons.database.specifications.JpaRepository;
import centralworks.spawners.modules.models.quests.PlayerQuests;

public class JpaUserQuestsRepository extends JpaRepository<PlayerQuests, String> {

    private static JpaUserQuestsRepository repository;

    public static JpaUserQuestsRepository require() {
        return repository == null ? repository = new JpaUserQuestsRepository() : repository;
    }

    public JpaUserQuestsRepository() {
        super(PlayerQuests.class);
    }
}
