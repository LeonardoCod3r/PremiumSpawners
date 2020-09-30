package centralworks.spawners;

public abstract class PluginSystem {

    public abstract String getId();
    public abstract void start(Main plugin);
    public abstract void terminate(Main plugin);

}
