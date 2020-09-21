package centralworks.spawners.modules.models.quests;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.specifications.QueryFunctions;
import centralworks.spawners.commons.database.repositories.UserQuestsRepository;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.modules.cmds.QuestsCommand;
import centralworks.spawners.modules.models.quests.cached.Interpreters;
import centralworks.spawners.modules.models.quests.cached.Quests;
import centralworks.spawners.modules.models.quests.cached.Runnables;
import centralworks.spawners.modules.models.quests.suppliers.CraftInterpreter;
import centralworks.spawners.modules.models.quests.suppliers.QuestLoader;
import lombok.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationQuest {
    private static ApplicationQuest applicationQuest;
    private Quests quests;
    private Interpreters interpreters;
    private Runnables runnablesToAward;

    public static ApplicationQuest get() {
        return applicationQuest;
    }

    public static void boot() {
        final QuestLoader questLoader = QuestLoader.get();
        questLoader.setDefaults();
        questLoader.run();
        applicationQuest = ApplicationQuest.builder()
                .interpreters(Interpreters.get())
                .quests(Quests.get())
                .runnablesToAward(Runnables.get()).build();
        applicationQuest.getInterpreters().loadDefaults();
        ((CraftServer) Main.get().getServer()).getCommandMap().register("quests", new QuestsCommand());
    }

    public static void shutdown() {
        final SyncRequests<PlayerQuests, String> q = SyncRequests.supply(UserQuestsRepository.require());
        QueryFunctions.saveAll(q.getRepository());
        /*q.getDto().delete();*/
    }

    public void registerRunnable(String id, Consumer<String> consumer) {
        runnablesToAward.add(Runnables.QuestRunnableReward.builder().id(id).toReward(consumer).build());
    }

    public <T extends Event> void registerInterpreter(Listener listener, String id) {
        final CraftInterpreter<T> interpreter = new CraftInterpreter<>(id);
        interpreter.setListener(listener);
        interpreter.commit();
    }

}
