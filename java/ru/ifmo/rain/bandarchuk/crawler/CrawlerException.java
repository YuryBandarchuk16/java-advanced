package ru.ifmo.rain.bandarchuk.crawler;

public class CrawlerException extends Exception {

    public CrawlerException(String message) {
        super(message);
    }

    public CrawlerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
