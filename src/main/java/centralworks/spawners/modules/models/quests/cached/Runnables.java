package centralworks.spawners.modules.models.quests.cached;

import centralworks.spawners.lib.Cache;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

public class Runnables extends Cache<Runnables.QuestRunnableReward> {

    private static Runnables me;

    public static Runnables get() {
        return me == null ? me = new Runnables() : me;
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestRunnableReward {
        private String id;
        private Consumer<String> toReward;
    }

}