package ru.ifmo.rain.bandarchuk.crawler;

public interface ThreadPool extends AutoCloseable {

    void addTask(Runnable task);

    void close();
}
