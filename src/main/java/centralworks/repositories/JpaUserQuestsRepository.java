package centralworks.repositories;

import centralworks.Main;
import centralworks.database.JpaRepository;
import centralworks.quests.models.PlayerQuests;

public class JpaUserQuestsRepository extends JpaRepository<PlayerQuests, String> {

    private static JpaUserQuestsRepository repository;

    public JpaUserQuestsRepository() {
        super(PlayerQuests.class);
    }

    public static JpaUserQuestsRepository require() {
        return repository == null ? repository = Main.getInstance().getInjector().getInstance(JpaUserQuestsRepository.class) : repository;
    }
}
