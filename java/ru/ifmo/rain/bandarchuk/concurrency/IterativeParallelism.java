package ru.ifmo.rain.bandarchuk.concurrency;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {

    @Override
    public String join(int threads, List<?> elements) throws InterruptedException {
        Function<Stream<?>, String> map = s -> s.map(Object::toString).collect(Collectors.joining());
        Function<Stream<String>, String> reduce = s -> s.collect(Collectors.joining());
        return MapReduceManager.work(threads, elements, map, reduce);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> elements, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, List<T>> map = s -> s.filter(predicate).collect(Collectors.toList());
        Function<Stream<List<T>>, List<T>> reduce = s -> s.flatMap(List::stream).collect(Collectors.toList());
        return MapReduceManager.work(threads, elements, map, reduce);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> elements, Function<? super T, ? extends U> mapper) throws InterruptedException {
        Function<Stream<? extends T>, List<U>> map = s -> s.map(mapper).collect(Collectors.toList());
        Function<Stream<List<U>>, List<U>> reduce = s -> s.flatMap(List::stream).collect(Collectors.toList());
        return MapReduceManager.work(threads, elements, map, reduce);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> elements, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> function = s -> s.max(comparator).get();
        return MapReduceManager.work(threads, elements, function, function);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> elements, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> function = s -> s.min(comparator).get();
        return MapReduceManager.work(threads, elements, function, function);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> elements, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, Boolean> map = s -> s.allMatch(predicate);
        Function<Stream<Boolean>, Boolean> reduce = s -> s.allMatch(val -> val);
        return MapReduceManager.work(threads, elements, map, reduce);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> elements, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, Boolean> map = s -> s.anyMatch(predicate);
        Function<Stream<Boolean>, Boolean> reduce = s -> s.anyMatch(val -> val);
        return MapReduceManager.work(threads, elements, map, reduce);
    }
}
