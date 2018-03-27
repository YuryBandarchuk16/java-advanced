package ru.ifmo.rain.bandarchuk.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Reducer<T> {
    private final List<T> values;
    private final Function<? super Stream<T>, T> reducer;

    public Reducer(Function<? super Stream<T>, T> reducer) {
        this.reducer = reducer;
        values = new ArrayList<>();
    }

    public void add(T value) {
        values.add(value);
    }

    public T reduce() {
        return reducer.apply(values.stream());
    }
}
