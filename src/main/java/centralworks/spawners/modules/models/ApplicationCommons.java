package centralworks.spawners.modules.models;

import centralworks.spawners.Main;
import centralworks.spawners.commons.database.specifications.QueryFunctions;
import centralworks.spawners.commons.database.specifications.Repository;
import centralworks.spawners.commons.database.repositories.jpa.JpaUserRepository;
import centralworks.spawners.commons.database.SyncRequests;
import centralworks.spawners.modules.hook.PlaceHolderHook;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.addons.ImpulseLoader;
import centralworks.spawners.modules.models.addons.LimitLoader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationCommons {

    public static void boot() {
        final Repository<UserDetails, String> dao = SyncRequests.supply(JpaUserRepository.require()).getRepository();
        QueryFunctions.loadAll(dao);
        MenusSettings.get();
        final ImpulseLoader impulseLoader = ImpulseLoader.get();
        impulseLoader.setDefaults();
        impulseLoader.run();
        if (Main.get().limitSystemIsActive()) {
            final LimitLoader limitLoader = LimitLoader.get();
            limitLoader.setDefaults();
            limitLoader.run();
            new PlaceHolderHook().register();
        }
    }

    public static void shutdown() {
        final SyncRequests<UserDetails, String> q = SyncRequests.supply(JpaUserRepository.require());
        QueryFunctions.saveAll(q.getRepository());
        /*q.getDto().delete();*/
    }

}
