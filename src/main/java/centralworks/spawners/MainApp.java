package centralworks.spawners;

import centralworks.spawners.test.Testing;
import centralworks.spawners.test.TestingRepositoryImpl;

import java.util.Arrays;

public class MainApp{

    public static void main(String[] args) {
        // test code
        final TestingRepositoryImpl repository = new TestingRepositoryImpl();
        repository.create(Testing.builder().name("dsada").lista(Arrays.asList("oi", "caralho")).nickname("dsadas").years(32).build());
    }

}
