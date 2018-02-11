package ru.ifmo.rain.bandarchuk.walk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FNV32Hash {

    private static final long INIT  = 0x811c9dc5;
    private static final long PRIME = 0x01000193;
    private static final long MODULE = (1L << 32L);

    private static final int BUFF_SIZE = 1024;

    private static long getHash(final long init, final int length, final byte[] bytes) {
        long result = init;
        for (int index = 0; index < length; index++) {
            result = ((result * PRIME) % MODULE) ^ Byte.toUnsignedLong(bytes[index]);
        }
        return result;
    }

    public static int getHash(final Path filePath) {
        long result = INIT;
        try (InputStream inputStream = new FileInputStream(filePath.toString())) {
            int countRead;
            byte[] buff = new byte[BUFF_SIZE];
            while ((countRead = inputStream.read(buff)) >= 0) {
                result = getHash(result, countRead, buff);
            }
        } catch (IOException e) {
            result = 0;
        }
        return (int)result;
    }
}
