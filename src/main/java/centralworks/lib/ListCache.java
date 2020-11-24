package centralworks.lib;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListCache<T> {

    private List<T> list = Lists.newArrayList();

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public void add(T object) {
        list.add(object);
    }

    public void remove(T object) {
        final List<T> ts = Lists.newArrayList(list);
        ts.remove(object);
        setList(ts);
    }

    public boolean exists(Predicate<T> query) {
        return list.stream().anyMatch(query);
    }

    public T get(Predicate<T> query) {
        return list.stream().filter(query).findFirst().get();
    }

    public List<T> getAll(Predicate<T> query) {
        return list.stream().filter(query).collect(Collectors.toList());
    }

    public void remove(Predicate<T> query) {
        remove(list.stream().filter(query).findFirst().get());
    }

    public void removeAll(List<T> objects) {
        list.removeAll(objects);
    }

    public void removeAll(Predicate<T> query) {
        list.removeIf(query);
    }

    public void clear() {
        list.clear();
    }


}
