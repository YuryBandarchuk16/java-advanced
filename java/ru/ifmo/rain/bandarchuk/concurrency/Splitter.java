package ru.ifmo.rain.bandarchuk.concurrency;

import java.util.ArrayList;
import java.util.List;

public class Splitter {
    public static <T> List<List<? extends T>> split(final int parts, final List<? extends T> elements) {
        if (parts <= 0) {
            throw new IllegalArgumentException("Invalid argument: parts = " + parts + ", can not be less than zero");
        }
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Invalid elements");
        }
        int elementsSize = elements.size();
        int chunkSize = Math.max(1, elementsSize / parts);
        int tailSize = elementsSize - chunkSize * parts;
        List<List<? extends T>> result = new ArrayList<>();

        int index = 0;
        while (index < elementsSize) {
            int currentChunkSize = chunkSize;
            if (tailSize > 0) {
                tailSize--;
                currentChunkSize++;
            }

            result.add(elements.subList(index, Math.min(elementsSize, index + currentChunkSize)));
            index += currentChunkSize;
        }

        return result;
    }
}
