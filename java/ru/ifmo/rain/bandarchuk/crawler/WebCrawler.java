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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;

public class WebCrawler implements Crawler {

    private static final int DEFAULT_DEPTH = 2;
    private static final int DEFAULT_DOWNLOADERS = 10;
    private static final int DEFAULT_EXTRACTORS = 10;
    private static final int DEFAULT_CONNECTIONS_PER_HOST = 10;

    private final ThreadPool downloadersPool;
    private final ThreadPool extractorsPool;
    private final Downloader downloader;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloadersPool = new ThreadPoolImpl(downloaders);
        extractorsPool  = new ThreadPoolImpl(extractors);
        this.downloader = downloader;
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> downloaded =  ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();

        final Phaser phaser = new Phaser(1);
        downloaded.add(url);
        traverse(url, depth, downloaded, errors, phaser);
        phaser.arriveAndAwaitAdvance();

        downloaded.removeAll(errors.keySet());
        return new Result(new ArrayList<>(downloaded), errors);
    }

    private void traverse(final String url, final int remainingDepth, final Set<String> downloaded,
                          final Map<String, IOException> errors, final Phaser phaser) {
        if (remainingDepth > 0) {
            Runnable downloadTask = () -> {
                try {
                    final Document document = downloader.download(url);
                    Runnable extractTask = () -> {
                        try {
                            List<String> newLinks = document.extractLinks();
                            newLinks.forEach(newLink -> {
                                if (!downloaded.contains(newLink)) {
                                    downloaded.add(newLink);
                                    traverse(newLink, remainingDepth - 1, downloaded, errors, phaser);
                                }
                            });
                        } catch (IOException e) {
                            errors.put(url, e);
                        } finally {
                            phaser.arrive();
                        }
                    };
                    if (remainingDepth > 1) {
                        phaser.register();
                        extractorsPool.addTask(extractTask);
                    }
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arrive();
                }
            };
            phaser.register();
            downloadersPool.addTask(downloadTask);
        }
    }

    @Override
    public void close() {
        downloadersPool.close();
        extractorsPool.close();
    }

    public static void main(String[] args) throws IOException {
        try {
            if (args.length == 0) {
                throw new CrawlerException("Usage:" +
                                           "       url [depth [downloaders [extractors [perHost]]]]");
            }
            String url      = args[0];
            int depth       = Utils.getArgument(args, 1, Integer::parseInt, DEFAULT_DEPTH);
            int downloaders = Utils.getArgument(args, 2, Integer::parseInt, DEFAULT_DOWNLOADERS);
            int extractors  = Utils.getArgument(args, 3, Integer::parseInt, DEFAULT_EXTRACTORS);
            int perHost     = Utils.getArgument(args, 4, Integer::parseInt, DEFAULT_CONNECTIONS_PER_HOST);

            Downloader downloader = new CachingDownloader();
            WebCrawler crawler = new WebCrawler(downloader, downloaders, extractors, perHost);
            Result result = crawler.download(url, depth);

            System.out.println("Successfully downloaded: " + result.getDownloaded().size());
            result.getDownloaded().forEach(System.out::println);
            System.out.println("Not downloaded due to error: " + result.getErrors().size());
            result.getErrors().forEach((s, e) -> {
                System.out.println("URL: " + s);
                System.out.println("Error: " + e.getMessage());
            });
        } catch (CrawlerException e) {
            System.out.println(e.getMessage());
        }
    }
}
