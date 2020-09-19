package centralworks.spawners.commons.database.repositories;

import centralworks.spawners.commons.database.specifications.CrudRepository;
import centralworks.spawners.modules.models.quests.PlayerQuests;

public class UserQuestsRepository extends CrudRepository<PlayerQuests, String> {

    private static UserQuestsRepository repository;

    public static UserQuestsRepository require() {
        return repository == null ? repository = new UserQuestsRepository() : repository;
    }

    public UserQuestsRepository() {
        super(PlayerQuests.class);
    }
}
