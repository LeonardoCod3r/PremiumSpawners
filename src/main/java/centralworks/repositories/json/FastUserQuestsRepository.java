package centralworks.repositories.json;

import centralworks.core.quests.models.PlayerQuests;
import centralworks.database.specifications.FastRepository;

public class FastUserQuestsRepository extends FastRepository<PlayerQuests, String> {

    private static FastUserQuestsRepository repository;

    public FastUserQuestsRepository() {
        super(PlayerQuests.class);
    }

    public static FastUserQuestsRepository require() {
        return repository == null ? repository = new FastUserQuestsRepository() : repository;
    }
}
