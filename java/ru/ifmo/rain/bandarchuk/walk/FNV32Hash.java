package ru.ifmo.rain.bandarchuk.walk;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FNV32Hash {

    private static final int INIT  = 0x811c9dc5;
    private static final int PRIME = 0x01000193;

    private static final int BUFF_SIZE = 1024;

    private static int getHash(final int init, final int length, final byte[] bytes) {
      int result = init;
      for (int index = 0; index < length; index++) {
        result = (result * PRIME) ^ Byte.toUnsignedInt(bytes[index]);
      }
      return result;
    }

    public static int getHash(final Path filePath) {
      try (InputStream inputStream = new FileInputStream(filePath.toString())) {
        int result = INIT;
        int countRead;
        byte[] buff = new byte[BUFF_SIZE];
        while ((countRead = inputStream.read(buff)) >= 0) {
          result = getHash(result, countRead, buff);
        }
        return result;
      } catch (IOException e) {
        return 0;
      }
    }
}
