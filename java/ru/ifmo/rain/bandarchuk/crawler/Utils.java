package ru.ifmo.rain.bandarchuk.crawler;

import java.util.function.Function;

public class Utils {

    public static <T> T getArgument(String[] args, int index, Function<String, T> mapper, T defaultValue) {
        if (index < args.length) {
            return mapper.apply(args[index]);
        }
        return defaultValue;
    }
}
