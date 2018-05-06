package ru.ifmo.rain.bandarchuk.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolImpl implements ThreadPool {

    private final ExecutorService tasksPool;

    public ThreadPoolImpl(int maxThreadNum) {
        tasksPool = Executors.newFixedThreadPool(maxThreadNum);
    }

    @Override
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
