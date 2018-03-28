package ru.ifmo.rain.bandarchuk.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapReduceManager {
    public static <T, R> R work(int threads, final List<? extends T> elements,
                                final Function<Stream<? extends T>, R> map,
                                final Function<? super Stream<R>, R> reduce) throws InterruptedException {
        Reducer<R> reducer = new Reducer<>(reduce);
        List<Worker<T, R>> workers = MapReduceManager.assign(Splitter.split(threads, elements), map);

        for (Worker<T, R> worker: workers) {
            reducer.add(worker.getResult());
        }

        return reducer.reduce();
    }

    private static <T, R> List<Worker<T, R>> assign(final List<List<? extends T>> tasks,
                                              final Function<Stream<? extends T>, R> map) {
        List<Worker<T, R>> workers = new ArrayList<>();

        for(List<? extends T> task: tasks) {
            workers.add(new Worker<>(map, task.stream()));
        }

        return workers;
    }

}
