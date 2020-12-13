package centralworks.spawners.models;

import centralworks.Main;
import centralworks.cache.google.Caches;
import centralworks.models.User;
import centralworks.models.enums.ImpulseType;
import centralworks.events.SpawnerStackEvent;
import centralworks.database.Storable;
import centralworks.database.specifications.BindRepository;
import centralworks.database.specifications.Repository;
import centralworks.hooks.DynmapHook;
import centralworks.lib.BalanceFormatter;
import centralworks.lib.LocationUtils;
import centralworks.lib.Settings;
import centralworks.lib.enums.EntityName;
import centralworks.repositories.json.FastSpawnerRepository;
import centralworks.repositories.mysql.JpaSpawnerRepository;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Data
public class Spawner extends Storable<Spawner> implements Serializable {

    @Id
    @Column(length = 150)
    @Expose
    private String locSerialized;
    @Expose
    private String owner;
    @Expose
    private String entityTypeSerialized;
    @Expose
    private Double amount = 1.0;
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "locSerialized")
    @Expose
    private List<String> friends = Lists.newLinkedList();
    @OneToMany(mappedBy = "spawner", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Expose
    private List<SpawnerImpulse> impulsesOfGeneration = Lists.newLinkedList();
    @Expose
    private Long hologramId;
    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "locSerialized")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Expose
    private Statistics statistics;

    public Spawner(String locSerialized) {
        this.locSerialized = locSerialized;
        this.statistics = new Statistics(this);
    }

    public Spawner(Location location) {
        setLocation(location);
        this.statistics = new Statistics(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Repository<Spawner, String> getRepository() {
        final BindRepository<Spawner, String> bindRepository = new BindRepository<>(Spawner.class, JpaSpawnerRepository.require(), FastSpawnerRepository.require());
        return bindRepository.getRelativeRepository();
    }

    @Override
    public Object getEntityIdentifier() {
        return this.locSerialized;
    }

    public Double getPrice() {
        return Main.getInstance().getSpawners().navigate().getDouble("List." + getEntityType().toString() + ".price");
    }

    public Double getPriceAll() {
        return getPrice() * getAmount();
    }

    public User getUser() {
        return Caches.getCache(User.class).getIfPresent(owner);
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

    public Location getLocation() {
        return LocationUtils.stringToLoc(getLocSerialized());
    }

    public void setLocation(Location location) {
        this.locSerialized = LocationUtils.locToString(location);
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
        var messages = Main.getInstance().getMessages().navigate();
        for (SpawnerImpulse si : getImpulsesOfGeneration()) {
            si.fix().setValid(true);
            si.run(this, () -> {
                if (isOnlinePlayer())
                    getPlayer().sendMessage(messages.getMessage("boosterEnd").replace("{type}", si.getImpulseType().name()));
            });
        }
    }

    public void impulsesForceStop() {
        getImpulsesOfGeneration().forEach(SpawnerImpulse::stop);
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
        final Settings.Navigate nav = Main.getInstance().getSpawners().navigate();
        final boolean hologram = nav.getBoolean("HologramToggle");
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            final Location l = getLocation();
            l.getBlock().setType(Material.MOB_SPAWNER);
            final CreatureSpawner spawnerBlock = ((CreatureSpawner) l.getBlock().getState());
            spawnerBlock.setCreatureTypeByName(getEntityType().name());
            spawnerBlock.setSpawnedType(getEntityType());
            spawnerBlock.setDelay(20);
            spawnerBlock.update();
            updateHologram(hologram);
            impulsesForceRun();
            if (callback != null) callback.accept(this);
            final DynmapHook dynmapHook = Main.getInstance().getDynmapHook();
            dynmapHook.view(this);
        });
    }

    public void pullHologram() {
        final Settings.Navigate nav = Main.getInstance().getSpawners().navigate();
            final Hologram hologram = HologramsAPI.createHologram(Main.getInstance(), getLocation().add(0.0, 2.50, 0.0).add(0.5, 0.0, 0.5));
            setHologramId(hologram.getCreationTimestamp());
            nav.getColorfulList("Hologram").forEach(s -> hologram.appendTextLine(s
                    .replace("{mob}", getEntityName())
                    .replace("{stack}", BalanceFormatter.format(getAmount()))
                    .replace("{owner}", getOwner()))
            );
            hologram.teleport(hologram.getLocation());

        dynmapShow();
    }

    public void dynmapShow() {
        final DynmapHook dynmapHook = Main.getInstance().getDynmapHook();
        dynmapHook.view(this);
    }

    public void dynmapHide() {
        final DynmapHook dynmapHook = Main.getInstance().getDynmapHook();
        dynmapHook.hide(this);
    }

    public void updateHologram(boolean isActive) {
        final Collection<Hologram> holograms = HologramsAPI.getHolograms(Main.getInstance());
        final Settings.Navigate nav = Main.getInstance().getSpawners().navigate();
        if (isActive) {
            if (getHologramId() != null && holograms.stream().anyMatch(hologram -> hologram.getCreationTimestamp() == getHologramId())) {
                final Hologram hologram = HologramsAPI.getHolograms(Main.getInstance()).stream().filter(h -> h.getCreationTimestamp() == getHologramId()).findFirst().get();
                hologram.clearLines();
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    nav.getColorfulList("Hologram").forEach(s -> hologram.appendTextLine(s
                            .replace("{mob}", getEntityName())
                            .replace("{stack}", BalanceFormatter.format(getAmount()))
                            .replace("{owner}", getOwner()))
                    );
                    hologram.teleport(hologram.getLocation());
                }, 2L);
            } else pullHologram();
        }
        dynmapShow();
    }

    public Boolean isOwner(String name) {
        return getOwner().equalsIgnoreCase(name);
    }

    public Boolean hasPermission(String name) {
        return getOwner().equalsIgnoreCase(name) || getFriends().stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }

    public void destroy(User user) {
        HologramsAPI.getHolograms(Main.getInstance()).stream().filter(h -> h.getCreationTimestamp() == getHologramId()).findFirst().ifPresent(Hologram::delete);
        setHologramId(null);
        final Location l = getLocation();
        l.getBlock().setType(Material.AIR);
        impulsesOfGeneration.forEach(SpawnerImpulse::stop);
        final LoadingCache<String, Spawner> cache = Caches.getCache(Spawner.class);
        cache.invalidate(getLocSerialized());
        query().delete();
        if (user!=null) user.deleteSpawnerLocation(l);
        else getUser().deleteSpawnerLocation(l);
        dynmapHide();
    }

    public void concat(Spawner spawner) {
        if (spawner.getEntityType() != getEntityType()) return;
        addStack(spawner.getAmount());
        addFriend(spawner.getFriends());
        spawner.getImpulsesOfGeneration().forEach(i -> {
            i.stop();
            i.in(this);
        });
        final Settings.Navigate nav = Main.getInstance().getSpawners().navigate();
        final boolean hologram = nav.getBoolean("HologramToggle");
        updateHologram(hologram);
    }

    public Boolean concat(Player p, ItemStack item) {
        try {
            final SpawnerItem spawnerItem = new SpawnerItem(item);
            if (spawnerItem.getEntityType() != getEntityType()) return false;
            final Spawner spawner = new SpawnerBuilder().build(spawnerItem);
            spawner.setOwner(p.getName());
            final SpawnerStackEvent event = new SpawnerStackEvent(p, this, spawner);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            concat(spawner);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean canConcat(ItemStack item) {
        try {
            final SpawnerItem spawnerItem = new SpawnerItem(item);
            return spawnerItem.getEntityType() == getEntityType();
        } catch (Exception ignored) {
            return false;
        }
    }
}
