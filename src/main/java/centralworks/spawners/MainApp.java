package centralworks.spawners;

import centralworks.spawners.test.Testing;
import centralworks.spawners.test.TestingRepositoryImpl;

import java.util.ArrayList;

public class MainApp {

    public static void main(String[] args) {
        final TestingRepositoryImpl repository = new TestingRepositoryImpl();
        final int times = 10000;
        for (int i = times; i != 0; i--) {
            final Testing testing = Testing.builder().nickname("LeonardoCd3r").years(14).lista(new ArrayList<>()).build();
            repository.create(testing);
            testing.setYears(i);
            repository.update(testing);
            repository.delete(testing);
        }
    }

}
