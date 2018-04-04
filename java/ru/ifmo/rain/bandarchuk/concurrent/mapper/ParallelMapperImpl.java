package ru.ifmo.rain.bandarchuk.concurrent.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threads;
    private final Queue<Runnable> tasks;

    public ParallelMapperImpl(int threads) {
        this.threads = new ArrayList<>(threads);
        this.tasks = new LinkedList<>();

        Runnable runnable = () -> {
            try {
                while (!Thread.interrupted()) {
                    Runnable task;
                    synchronized (tasks) {
                        while (tasks.size() == 0) {
                            tasks.wait();
                        }
                        task = tasks.poll();
                    }
                    if (task != null) {
                        task.run();
                    }
                }
            } catch (InterruptedException ignored) {
            }
        };

        while (threads-- > 0) {
            this.threads.add(new Thread(runnable));
        }
        applyOnThreads(Thread::start, t -> !t.isAlive());
    }

    private void applyOnThreads(Consumer<? super Thread> action, Predicate<? super Thread> condition) {
        threads.stream().filter(condition).forEach(action);
    }

    private final class SelfNotifiableCounter {

        private int currentValue;
        private int targetValue;

        private boolean isReady = false;

        public SelfNotifiableCounter(int initialValue, int targetValue) {
            this.currentValue = initialValue;
            this.targetValue = targetValue;
            if (initialValue == targetValue) {
                throw new IllegalArgumentException("Invalid arguments: target value should be equal to initial value");
            }
        }

        public void add(int x) {
            currentValue += x;
            if (currentValue == targetValue) {
                isReady = true;
                this.notifyAll();
            }
        }

        private boolean isReady() {
            return isReady;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        int elementsCount = args.size();

        List<R> result = new ArrayList<>(Collections.nCopies(elementsCount, null));

        final SelfNotifiableCounter counter = new SelfNotifiableCounter(0, elementsCount);
        for (int index = 0; index < elementsCount; index++) {
            final int unmodifiableIndex = index;
            Runnable runnable = () -> {
                result.set(unmodifiableIndex, f.apply(args.get(unmodifiableIndex)));
                synchronized (counter) {
                    counter.add(1);
                }
            };
            synchronized (tasks) {
                tasks.add(runnable);
                tasks.notify();
            }
        }

        synchronized (counter) {
            while (!counter.isReady()) {
                counter.wait();
            }
        }

        return result;
    }

    @Override
    public void close() {
        applyOnThreads(Thread::interrupt, t -> !t.isInterrupted());
        applyOnThreads(t -> {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }, t -> true);
    }
}
