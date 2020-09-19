package centralworks.spawners.modules.models.spawners;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.specifications.Repository;
import centralworks.spawners.commons.database.repositories.SpawnerRepository;
import centralworks.spawners.commons.database.Storable;
import centralworks.spawners.lib.Configuration;
import centralworks.spawners.lib.EntityName;
import centralworks.spawners.lib.FormatBalance;
import centralworks.spawners.lib.Serialize;
import centralworks.spawners.modules.animations.AnimationService;
import centralworks.spawners.modules.hook.DynmapHook;
import centralworks.spawners.modules.models.UserDetails;
import centralworks.spawners.modules.models.addons.ImpulseType;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class Spawner extends Storable<Spawner> implements Serializable {

    @Id
    @Column(length = 150)
    @Getter
    @Setter
    private String locSerialized;
    @Getter
    @Setter
    private String owner;
    @Getter
    @Setter
    private String entityTypeSerialized;
    @Getter
    @Setter
    private Double amount = 1.0;
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinColumn(name = "locSerialized")
    @Getter
    @Setter
    private List<String> friends = Lists.newLinkedList();
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "locSerialized")
    @Getter
    @Setter
    private List<SpawnerImpulse> impulsesOfGeneration = Lists.newLinkedList();
    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "locSerialized")
    @Getter
    @Setter
    private AnimationService animationService;
    @Getter
    private final transient Repository<Spawner, String> repository = SpawnerRepository.require();
    @Getter
    @Setter
    private Long hologramId;

    public Spawner(String locSerialized) {
        this.locSerialized = locSerialized;
    }

    public Spawner(Location location) {
        setLocation(location);
    }

    @Override
    public Object getIdentifier() {
        return this.locSerialized;
    }

    @Override
    public void setIdentifier(Object object) {
        this.locSerialized = object.toString();
    }

    public Double getPrice() {
        return Main.getSpawners().getDouble("List." + getEntityType().toString() + ".price");
    }

    public Double getPriceAll() {
        return getPrice() * getAmount();
    }

    public void addFriend(String friend) {
        friends.add(friend);
    }

    public void addFriend(String... friends) {
        this.friends.addAll(Lists.newArrayList(friends));
    }

    public void addFriend(List<String> friends) {
        this.friends.addAll(Lists.newArrayList(friends));
    }

    public void removeFriend(String friend) {
        friends.remove(friend);
    }

    public boolean existsFriend(String friend) {
        return friends.stream().anyMatch(s -> s.equalsIgnoreCase(friend));
    }

    public void addStack(Double value) {
        this.amount += value;
    }

    public void removeStack(Double value) {
        this.amount -= value;
    }

    public String getEntityName() {
        return EntityName.valueOf(getEntityType()).getName();
    }

    public boolean inAnimation() {
        return animationService != null;
    }

    public Location getLocation() {
        final Serialize<String, Location> se = new Serialize<>(this.locSerialized);
        return se.getResult();
    }

    public void setLocation(Location location) {
        final Serialize<Location, String> se = new Serialize<>(location.clone());
        this.locSerialized = se.getResult();
    }

    public EntityType getEntityType() {
        return EntityType.valueOf(this.entityTypeSerialized);
    }

    public void setEntityType(EntityType entityType) {
        this.entityTypeSerialized = entityType.toString();
    }

    public void removeImpulse(SpawnerImpulse impulse) {
        if (impulse.getImpulseType() == ImpulseType.GENERATION)
            CompletableFuture.supplyAsync(() -> impulsesOfGeneration.stream().filter(impulse1 -> impulse1.getIdTask().equals(impulse.getIdTask())).findFirst())
                    .thenAccept(impulse1 -> impulse1.ifPresent(value -> impulsesOfGeneration.remove(value)));
    }

    public void impulsesForceRun() {
        final Configuration messages = Main.getMessages();
        for (SpawnerImpulse si : getImpulsesOfGeneration()) {
            si.fix().setValid(true);
            si.run(this, () -> {
                if (isOnlinePlayer())
                    getPlayer().sendMessage(messages.getMessage("boosterEnd").replace("{type}", si.getImpulseType().name()));
            });
        }
        query().commit();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(owner);
    }

    public Boolean isOnlinePlayer() {
        return getPlayer() != null;
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(owner);
    }

    public boolean hasImpulse() {
        return getImpulsesOfGeneration().size() > 0;
    }

    public void removeImpulse(SpawnerImpulse impulse, Runnable callback) {
        if (impulse.getImpulseType() == ImpulseType.GENERATION)
            impulsesOfGeneration.stream()
                    .filter(impulse1 -> impulse1.getIdTask().equals(impulse.getIdTask())).findFirst()
                    .ifPresent(value -> {
                        impulsesOfGeneration.remove(value);
                        callback.run();
                    });
    }

    public void addImpulse(SpawnerImpulse impulse) {
        if (impulse.getImpulseType() == ImpulseType.GENERATION) {
            impulsesOfGeneration.add(impulse);
        }
    }

    public boolean hasBoosterActive(ImpulseType impulseType) {
        return getImpulsesOfGeneration().stream().anyMatch(i -> i.getImpulseType() == impulseType && i.isValid());
    }

    public boolean hasBooster(ImpulseType impulseType) {
        return getImpulsesOfGeneration().stream().anyMatch(i -> i.getImpulseType() == impulseType);
    }

    public Double getMultiplierOf(ImpulseType type) {
        return getImpulsesOfGeneration().stream().filter(s -> s.getImpulseType() == type).map(SpawnerImpulse::getValue).reduce(Double::sum).orElse(1.0);
    }

    public void appear(Consumer<Spawner> callback) {
        Bukkit.getScheduler().runTask(Main.get(), () -> {
            final Location l = getLocation();
            l.getBlock().setType(Material.MOB_SPAWNER);
            final CreatureSpawner spawnerBlock = ((CreatureSpawner) l.getBlock().getState());
            spawnerBlock.setCreatureTypeByName(getEntityType().name());
            spawnerBlock.setSpawnedType(getEntityType());
            spawnerBlock.setDelay(20);
            spawnerBlock.update();
            pullHologram();
            impulsesForceRun();
            if (callback!=null) callback.accept(this);
            final DynmapHook dynmapHook = ApplicationSpawner.getDynmapHook();
            dynmapHook.view(this);
        });
    }

    public void pullHologram() {
        final Hologram hologram = HologramsAPI.createHologram(Main.get(), getLocation().add(0.0, 2.50, 0.0).add(0.5, 0.0, 0.5));
        setHologramId(hologram.getCreationTimestamp());
        Main.getSpawners().getList("Hologram", true).forEach(s -> hologram.appendTextLine(s
                .replace("{mob}", getEntityName())
                .replace("{stack}", FormatBalance.format(getAmount()))
                .replace("{owner}", getOwner()))
        );
        hologram.teleport(hologram.getLocation());
        final DynmapHook dynmapHook = ApplicationSpawner.getDynmapHook();
        dynmapHook.view(this);
    }

    public void updateHologram() {
        if (HologramsAPI.getHolograms(Main.get()).stream().anyMatch(hologram -> hologram.getCreationTimestamp() == getHologramId())) {
            final Hologram hologram = HologramsAPI.getHolograms(Main.get()).stream().filter(h -> h.getCreationTimestamp() == getHologramId()).findFirst().get();
            hologram.clearLines();
            Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
                Main.getSpawners().getList("Hologram", true).forEach(s -> hologram.appendTextLine(s
                        .replace("{mob}", getEntityName())
                        .replace("{stack}", FormatBalance.format(getAmount()))
                        .replace("{owner}", getOwner()))
                );
                hologram.teleport(hologram.getLocation());
            }, 2L);
        } else pullHologram();
        final DynmapHook dynmapHook = ApplicationSpawner.getDynmapHook();
        dynmapHook.view(this);
    }

    public Boolean isOwner(String name) {
        return getOwner().equalsIgnoreCase(name);
    }

    public Boolean hasPermission(String name) {
        return getOwner().equalsIgnoreCase(name) || getFriends().stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }

    public void destroy(UserDetails userDetails) {
        HologramsAPI.getHolograms(Main.get()).stream().filter(h -> h.getCreationTimestamp() == getHologramId()).findFirst().ifPresent(Hologram::delete);
        setHologramId(null);
        final Location l = getLocation();
        l.getBlock().setType(Material.AIR);
        impulsesOfGeneration.forEach(SpawnerImpulse::stop);
        query().delete(true);
        userDetails.deleteSpawnerLocation(l);
        userDetails.query().commit();
        final DynmapHook dynmapHook = ApplicationSpawner.getDynmapHook();
        dynmapHook.hide(this);
    }

    public void concat(Spawner spawner) {
        if (spawner.getEntityType() != getEntityType()) return;
        addStack(spawner.getAmount());
        addFriend(spawner.getFriends());
        spawner.getImpulsesOfGeneration().forEach(i -> {
            i.stop();
            i.go(this);
        });
        updateHologram();
    }

    public Boolean concat(ItemStack item) {
        try {
            final SpawnerItem spawnerItem = new SpawnerItem().parse(item);
            if (spawnerItem.getEntityType() != getEntityType()) return false;
            addStack(spawnerItem.getAmountItem() * spawnerItem.getAmountSpawners());
            updateHologram();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void cancelAnimation() {
        if (animationService==null) return;
        animationService.setCancelled(true);
        animationService = null;
        query().commit();
    }

}
