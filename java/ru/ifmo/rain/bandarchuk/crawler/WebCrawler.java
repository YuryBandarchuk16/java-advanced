package ru.ifmo.rain.bandarchuk.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Crawler {

    private static final int INITIAL_DEPTH = 1;

    private final ThreadPool downloadersPool;
    private final ThreadPool extractorsPool;
    private final Downloader downloader;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloadersPool = new ThreadPool(downloaders);
        extractorsPool  = new ThreadPool(extractors);
        this.downloader = downloader;
    }

    @Override
    public Result download(String url, int depth) {
        final AtomicInteger pendingTasks = new AtomicInteger(0);
        final Set<String> downloaded =  ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Map<String, Integer> visitedLinks = new ConcurrentHashMap<>();
        final Queue<String> pendingLinks = new ConcurrentLinkedQueue<>();

        pendingLinks.add(url);
        visitedLinks.put(url, INITIAL_DEPTH);
        while (true) {
            synchronized (pendingLinks) {
                while (pendingLinks.isEmpty() && pendingTasks.get() > 0) {
                    try {
                        pendingLinks.wait();
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
                if (pendingLinks.isEmpty() && pendingTasks.get() == 0) {
                    break;
                }
            }
            String link = pendingLinks.poll();
            downloaded.add(link);
            final int currentDepth = visitedLinks.get(link);
            final int newDepth = currentDepth + 1;
            if (currentDepth < depth) {
                Runnable downloadTask = () -> {
                    try {
                        final Document document = downloader.download(link);
                        Runnable extractTask = () -> {
                            try {
                                List<String> newLinks = document.extractLinks();
                                newLinks.forEach(newLink -> {
                                    if (!visitedLinks.containsKey(newLink)) {
                                        visitedLinks.put(newLink, newDepth);
                                        pendingLinks.add(newLink);
                                        synchronized (pendingLinks) {
                                            pendingLinks.notify();
                                        }
                                    }
                                });
                                pendingTasks.decrementAndGet();
                                synchronized (pendingLinks) {
                                    pendingLinks.notify();
                                }
                            } catch (IOException e) {
                                if (!errors.containsKey(link)) {
                                    errors.put(link, e);
                                }
                            }
                        };
                        extractorsPool.addTask(extractTask);
                    } catch (IOException e) {
                        if (!errors.containsKey(link)) {
                            errors.put(link, e);
                        }
                        pendingTasks.decrementAndGet();
                        synchronized (pendingTasks) {
                            pendingTasks.notify();
                        }
                    }
                };
                pendingTasks.incrementAndGet();
                downloadersPool.addTask(downloadTask);
            }
        }

        downloaded.removeAll(errors.keySet());
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public void close() {
        downloadersPool.close();
        extractorsPool.close();
    }

    public static void main(String[] args) throws IOException {
        WebCrawler crawler = new WebCrawler(new CachingDownloader(), 10, 10, 2);
        crawler.download("http://www.ifmo.ru", 1).getDownloaded().forEach(System.out::println);
    }
}
