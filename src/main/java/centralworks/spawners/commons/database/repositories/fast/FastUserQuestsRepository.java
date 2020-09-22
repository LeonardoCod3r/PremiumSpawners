package centralworks.spawners.commons.database.repositories.fast;

import centralworks.spawners.commons.database.specifications.FastRepository;
import centralworks.spawners.modules.models.quests.PlayerQuests;

public class FastUserQuestsRepository extends FastRepository<PlayerQuests, String> {

    private static FastUserQuestsRepository repository;

    public static FastUserQuestsRepository require() {
        return repository == null ? repository = new FastUserQuestsRepository() : repository;
    }

    public FastUserQuestsRepository() {
        super(PlayerQuests.class);
    }
}
