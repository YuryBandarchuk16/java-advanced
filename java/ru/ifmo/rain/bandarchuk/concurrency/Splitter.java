package ru.ifmo.rain.bandarchuk.concurrency;

import java.util.ArrayList;
import java.util.List;

public class Splitter {
    public static <T> List<List<? extends T>> split(final int parts, final List<? extends T> elements) {
        if (parts <= 0) {
            throw new IllegalArgumentException("Invalid argument: parts = " + parts + ", can not be less than zero");
        }
        int elementsSize = elements.size();
        int chunkSize = Math.max(1, elementsSize / parts);
        List<List<? extends T>> result = new ArrayList<>();

        for (int index = 0; index < elementsSize; index += chunkSize) {
            result.add(elements.subList(index, Math.min(elementsSize, index + chunkSize)));
        }

        return result;
    }
}
