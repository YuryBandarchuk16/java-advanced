package ru.ifmo.rain.bandarchuk.walk;

import ru.ifmo.rain.bandarchuk.walk.exceptions.RecursiveWalkException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {

    private final Path inputFilePath;
    private final Path outputFilePath;

    public RecursiveWalk(String inputFile, String outputFile) throws RecursiveWalkException {
        try {
            inputFilePath = Paths.get(inputFile);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException("Invalid input file at: '" + inputFile + "'");
        }
        try {
            outputFilePath = Paths.get(outputFile);
            if (!Files.exists(outputFilePath)) {
                try {
                    if (outputFilePath.getParent() != null) {
                        Files.createDirectories(outputFilePath.getParent());
                    }
                    Files.createFile(outputFilePath);
                } catch (IOException e) {
                    throw new RecursiveWalkException("Error while creating output file at: '" + outputFile + "'");
                }
            }
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException("Invalid output file: '" + outputFile + "'");
        }
    }

    public void walk() throws RecursiveWalkException {
        try (
             BufferedReader bufferedReader = Files.newBufferedReader(inputFilePath, StandardCharsets.UTF_8);
             PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFilePath.toString()), StandardCharsets.UTF_8)
            )
        ) {
            String nextLine;
            String previousLine = null;
            try {
                while ((nextLine = bufferedReader.readLine()) != null) {
                    try {
                        previousLine = nextLine;
                        Path path = Paths.get(nextLine);
                        if (Files.isDirectory(path)) {
                            Files.walkFileTree(path, new FileVisitor(printWriter));
                        } else {
                            printWriter.printf("%08x %s\n", FNV32Hash.getHash(path), path.toString());
                        }
                    } catch (InvalidPathException e) {
                        printWriter.printf("%08x %s\n", 0, nextLine);
                    } catch (IOException e) {
                        throw new RecursiveWalkException("Error while walking the file tree from: '" + nextLine + "'");
                    }
                }
            } catch (IOException e) {
                String message;
                if (previousLine == null) {
                    message = "at the beginning of the file";
                } else {
                    message = "after the following line:\n" + previousLine;
                }
                throw new RecursiveWalkException("Error while reading from file at: '" + inputFilePath.toString() + "'\n"
                                                + message);
            }
        } catch (IOException e) {
            boolean isInputFileCorrect = Files.isRegularFile(inputFilePath);
            boolean isOutputFileCorrect = Files.isRegularFile(outputFilePath);
            if (!isInputFileCorrect && !isOutputFileCorrect) {
                throw new RecursiveWalkException("Both input and output files are incorrect");
            } else if (!isInputFileCorrect) {
                throw new RecursiveWalkException("Input file is incorrect");
            } else if (!isOutputFileCorrect) {
                throw new RecursiveWalkException("Output file is incorrect");
            } else {
                throw new RecursiveWalkException("Error while working with input/output files");
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                throw new RecursiveWalkException("Usage: <input file path> <output file path>");
            }
            RecursiveWalk recursiveWalk = new RecursiveWalk(args[0], args[1]);
            recursiveWalk.walk();
        } catch (RecursiveWalkException e) {
            System.out.println(e.getMessage());
        }
    }
}
