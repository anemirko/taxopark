package ru.nemirko;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
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
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
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
            public FileVisitResult visitFileFailed(Path file, IOException exc){
                LOGGER.log(Level.WARNING, exc.getMessage());
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc){
                return CONTINUE;
            }
        });
    }
    /**
     * Создание тестового сообщения
     *
     * @param targetId id водителя
     */
    public static Message createMessage(Integer targetId){
        return new Message(targetId, Arrays.asList(" ", " ", " "));
    }
    /**
     * Создание тестового сообщения из xml
     *
     * @param xml строка с xml
     */
    public static Message messageFromXML(String xml) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Message.class);
        Unmarshaller um = jc.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        return (Message) um.unmarshal(reader);
    }
    /**
     * Создание xml из сообщения
     *
     * @param message сообщение
     */
    public static String messageToXML(Message message) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Message.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        marshaller.marshal(message, sw);
        return sw.toString();
    }
    /**
     * Создание файла xml из сообщения
     *
     * @param message сообщение
     * @param path путь сохранения файла
     */
    public static boolean MessageToFile(Message message, Path path) {
        try {
            Files.write(path, messageToXML(message).getBytes(), StandardOpenOption.CREATE_NEW);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return false;
    }



}
