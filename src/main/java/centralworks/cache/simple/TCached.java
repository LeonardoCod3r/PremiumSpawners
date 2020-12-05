package centralworks.cache.simple;

import centralworks.spawners.TaskType;
import centralworks.lib.ListCache;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class TCached extends ListCache<TCached.TaskObj> {

    private static TCached me;

    public static TCached get() {
        return me == null ? me = new TCached() : me;
    }

    @Data
    @RequiredArgsConstructor
    public static class TaskObj {

        private String playerName;
        private String value;
        private TaskType taskType;

        public TaskObj(String playerName, String value, TaskType taskType) {
            this.playerName = playerName;
            this.value = value;
            this.taskType = taskType;
        }
    }
}
