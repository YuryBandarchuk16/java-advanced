package ru.ifmo.rain.bandarchuk.concurrency;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapReduceManager {

    public static ParallelMapper parallelMapper = null;

    public static <T, R> R work(int threads, final List<? extends T> elements,
                                final Function<Stream<? extends T>, R> map,
                                final Function<? super Stream<R>, R> reduce) throws InterruptedException {


        if (parallelMapper != null) {
            return reduce.apply(parallelMapper.map(map, Splitter.split(threads, elements)).stream());
        }

        Reducer<R> reducer = new Reducer<>(reduce);
        List<Worker<T, R>> workers = MapReduceManager.assign(Splitter.split(threads, elements), map);

        InterruptedException exception = null;
        for (Worker<T, R> worker : workers) {
            try {
                reducer.add(worker.getResult());
            } catch (InterruptedException e) {
                exception = e;
            }
        }

        if (exception != null) {
            throw exception;
        }

        return reducer.reduce();
    }

    private static <T, R> List<Worker<T, R>> assign(final List<Stream<? extends T>> tasks,
                                                    final Function<Stream<? extends T>, R> map) {
        List<Worker<T, R>> workers = new ArrayList<>();

        for (Stream<? extends T> task : tasks) {
            workers.add(new Worker<>(map, task));
        }

        return workers;
    }

}
