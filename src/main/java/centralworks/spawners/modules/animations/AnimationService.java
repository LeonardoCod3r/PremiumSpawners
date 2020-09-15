package centralworks.spawners.modules.animations;

public interface AnimationService {

    boolean isCancelled();
    void setCancelled(boolean value);
    void setRadius(double value);
    double getRadius();
}
