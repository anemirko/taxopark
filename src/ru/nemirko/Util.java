package ru.nemirko;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;

/**
 * <p>Utility class</p>
 *
 * @author Alexey Nemirko
 */
public class Util {
    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    /**
     * Создает директорию или удаляет файлы в уже существующей
     *
     * @param path путь к создаваемой или очищаемой директории
     */
    public static void prepareDirectory(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Util.deleteFilesFromDirectory(path);
            } else {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    /**
     * Удаление файлов в директории
     *
     * @param path путь к очищаемой директории
     */
    public static void deleteFilesFromDirectory(Path path) throws IOException {
        walkFileTree(path, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                LOGGER.info(String.format("Directory: %s%n", dir));
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LOGGER.info(String.format("Delete file: %s%n", file));
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println(exc);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return CONTINUE;
            }
        });
    }
}
