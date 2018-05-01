package ru.ifmo.rain.bandarchuk.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool implements AutoCloseable {

    private final ExecutorService tasksPool;

    public ThreadPool(int maxThreadNum) {
        tasksPool = Executors.newFixedThreadPool(maxThreadNum);
    }

    public void addTask(Runnable task) {
        synchronized (tasksPool) {
            tasksPool.submit(task);
        }
    }

    @Override
    public void close() {
        tasksPool.shutdown();
    }
}
