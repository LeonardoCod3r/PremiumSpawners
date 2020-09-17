package centralworks.spawners.commons.di;

import centralworks.spawners.commons.di.services.ConnectionService;
import centralworks.spawners.commons.di.services.MainClassService;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class InjectBuilder<T> {

    private static final HashMap<Class<? extends AbstractModule>, Injector> injectors;

    static {
        injectors = Maps.newHashMap();
        register(new ConnectionService());
        register(new MainClassService());
    }

    private final Injector injector;
    private final T obj;

    public InjectBuilder(T obj, Class<? extends AbstractModule> clazz) {
        this.obj = obj;
        this.injector = injectors.get(clazz);
        checkFields();
        checkMethods();
    }

    public static void register(AbstractModule abstractModule) {
        injectors.put(abstractModule.getClass(), Guice.createInjector(abstractModule));
    }

    private void checkFields() {
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getAnnotation(com.google.inject.Inject.class) != null || field.getAnnotation(javax.inject.Inject.class) != null) {
                try {
                    boolean accessible;
                    if (accessible = !field.isAccessible()) field.setAccessible(true);
                    field.set(obj, injector.getInstance(field.getType()));
                    if (accessible) field.setAccessible(false);
                } catch (IllegalArgumentException | IllegalAccessException ignored) {
                }
            }
        }
    }

    private void checkMethods() {
        for (Method field : obj.getClass().getDeclaredMethods()) {
            if (field.getAnnotation(com.google.inject.Inject.class) != null || field.getAnnotation(javax.inject.Inject.class) != null) {
                try {
                    boolean accessible;
                    if (accessible = !field.isAccessible()) field.setAccessible(true);
                    field.invoke(obj);
                    if (accessible) field.setAccessible(false);
                } catch (Exception ignored) {
                }
            }
        }
    }

}
