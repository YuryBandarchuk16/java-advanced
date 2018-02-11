package ru.ifmo.rain.bandarchuk.walk;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileVisitor extends SimpleFileVisitor<Path> {

    private final PrintWriter printWriter;

    public FileVisitor(PrintWriter printWriter) {
      super();
      this.printWriter = printWriter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      printWriter.printf("%08x %s\n", FNV32Hash.getHash(file), file.toString());
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      printWriter.printf("%08x %s\n", 0, file.toString());
      return super.visitFileFailed(file, exc);
    }
}
