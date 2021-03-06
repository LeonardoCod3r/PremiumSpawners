package centralworks.cache.simple;

import centralworks.Main;
import centralworks.models.User;
import centralworks.repositories.JpaUserRepository;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class SpawnerRanking {

    private static SpawnerRanking me;
    private List<Supplier> suppliers = Lists.newArrayList();
    private boolean loaded = false;

    public static SpawnerRanking get() {
        return me == null ? me = new SpawnerRanking() : me;
    }

    public void updateAsync() {
        loaded = false;
        final long l = System.currentTimeMillis();
        CompletableFuture
                .supplyAsync(() -> JpaUserRepository.require().findAll())
                .thenApplyAsync(userDetails -> {
                    final List<Supplier> suppliers = Lists.newArrayList();
                    for (User user : userDetails) {
                        final double[] value = {0.0};
                        user.getSpawners().forEach(spawner -> value[0] += spawner.getPriceAll());
                        suppliers.add(new Supplier(user.getName(), value[0]));
                    }
                    return suppliers;
                })
                .thenAccept(suppliers -> {
                    setSuppliers(suppliers.stream().sorted((o1, o2) -> o2.getPriceAll().compareTo(o1.getPriceAll())).collect(Collectors.toList()));
                    loaded = true;
                    final long l1 = System.currentTimeMillis() - l;
                    final Logger logger = Main.getInstance().getLogger();
                    logger.log(Level.INFO, "O ranking de geradores foi carregado com sucesso.");
                    logger.log(Level.INFO, "Tempo de atualização: " + l1 + "ms.");
                });
    }

    @Data
    @RequiredArgsConstructor
    public static class Supplier {

        private String user;
        private Double priceAll;

        public Supplier(String user, Double priceAll) {
            this.user = user;
            this.priceAll = priceAll;
        }
    }
}
