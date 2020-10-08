package centralworks.repositories.json;

import centralworks.database.specifications.FastRepository;
import centralworks.core.quests.models.PlayerQuests;

public class FastUserQuestsRepository extends FastRepository<PlayerQuests, String> {

    private static FastUserQuestsRepository repository;

    public static FastUserQuestsRepository require() {
        return repository == null ? repository = new FastUserQuestsRepository() : repository;
    }

    public FastUserQuestsRepository() {
        super(PlayerQuests.class);
    }
}
