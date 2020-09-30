package centralworks.spawners.modules.hook;

import centralworks.spawners.Main;
import centralworks.spawners.lib.enums.PluginSystemType;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.Predicate;

public class EconomyContext {

    @Getter
    @Setter
    private static List<Context> contexts;

    static {
        final net.milkbowl.vault.economy.Economy eco = Main.getInstance().getVaultAPIEconomy();
        setContexts(Lists.newArrayList(new Context(Lists.newArrayList(PluginSystemType.DROPSTORAGE, PluginSystemType.QUESTS, PluginSystemType.SPAWNERS_BUY),
                new Economy() {
                    @Override
                    public void addMoney(String user, Double value) {
                        eco.depositPlayer(user, value);
                    }

                    @Override
                    public void removeMoney(String user, Double value) {
                        eco.withdrawPlayer(user, value);
                    }

                    @Override
                    public boolean hasMoney(String user, Double value) {
                        return eco.has(user, value);
                    }

                    @Override
                    public Double getBalance(String user) {
                        return eco.getBalance(user);
                    }

                    @Override
                    public boolean has(String user) {
                        return eco.hasAccount(user);
                    }

                })
        ));
    }

    public static void addContext(Economy economy, PluginSystemType... pluginSystems) {
        final Context context = new Context(Lists.newArrayList(pluginSystems), economy);
        addContext(context);
    }

    public static void addContext(Context context) {
        contexts.add(context);
    }

    public static Context getContext(PluginSystemType pluginSystem) {
        return contexts.stream().filter(context -> context.getSystems().contains(pluginSystem)).findFirst().get();
    }

    public static boolean hasContext(PluginSystemType pluginSystem) {
        return contexts.stream().anyMatch(context -> context.getSystems().contains(pluginSystem));
    }

    public static void removeContext(PluginSystemType pluginSystem) {
        removeContext(getContext(pluginSystem));
    }

    public static void removeContext(Context context) {
        contexts.remove(context);
    }

    public static boolean hasContext(Predicate<Context> predicate) {
        return contexts.stream().anyMatch(predicate);
    }

    public static Context getContext(Predicate<Context> predicate) {
        return contexts.stream().filter(predicate).findFirst().get();
    }

    public static void removeContext(Predicate<Context> predicate) {
        removeContext(getContext(predicate));
    }


    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Context {
        @Getter
        @Setter
        private List<PluginSystemType> systems;
        @Getter
        @Setter
        private Economy economy;
    }

    public abstract static class Economy {

        public abstract void addMoney(String user, Double value);

        public abstract void removeMoney(String user, Double value);

        public abstract boolean hasMoney(String user, Double value);

        public abstract Double getBalance(String user);

        public abstract boolean has(String user);

    }

}
