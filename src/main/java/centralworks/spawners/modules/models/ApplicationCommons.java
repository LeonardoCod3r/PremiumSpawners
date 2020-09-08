package centralworks.spawners.modules.models;

import centralworks.spawners.commons.database.specifications.AbstractDAO;
import centralworks.spawners.commons.database.QueriesSync;
import centralworks.spawners.modules.menu.settings.MenusSettings;
import centralworks.spawners.modules.models.addons.ImpulseLoader;
import centralworks.spawners.modules.models.addons.LimitLoader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationCommons {

    public static void boot(){
        final AbstractDAO<UserDetails> dao = QueriesSync.supply(UserDetails.class).getDao();
        dao.createTable();
        dao.loadAll();
        MenusSettings.get();
        final ImpulseLoader impulseLoader = ImpulseLoader.get();
        impulseLoader.setDefaults();
        impulseLoader.run();
        final LimitLoader limitLoader = LimitLoader.get();
        limitLoader.setDefaults();
        limitLoader.run();
    }

    public static void shutdown(){
        final QueriesSync<UserDetails> q = QueriesSync.supply(UserDetails.class);
        q.getDao().saveAll();
        q.getDto().delete();
    }

}
