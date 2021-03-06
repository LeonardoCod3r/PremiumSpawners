package centralworks;

public abstract class PluginSystem {

    public abstract String getId();

    public abstract void start(Main plugin, Object... objects);

    public abstract void reload(Main plugin);

    public abstract void terminate(Main plugin);

    public abstract boolean canRegister();

}
