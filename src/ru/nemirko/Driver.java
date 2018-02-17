package ru.nemirko;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
/**
 * Сущность водитель
 *
 * @author Alexey Nemirko
 */
public class Driver {
    private final ExecutorService service = Executors.newFixedThreadPool(1);
    private Integer id;
    private Path root;
    public static final int MONKEY_JOB = 3;  //3 секунды
    private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());

    public Driver(Integer id, Path root) {
        if (Files.isDirectory(root)) {
            this.id = id;
            this.root = root;
            LOGGER.info(String.format("Driver %d ready", id));
        } else {
            throw new IllegalArgumentException("You must specify the correct path to working directory for driver " + id);
        }
    }

    public Integer getId() {
        return id;
    }

    public synchronized Future<LocalDateTime> addMessage(Message message) {
        return service.submit(() -> {
            TimeUnit.SECONDS.sleep(MONKEY_JOB);
            Util.MessageToFile(message, Paths.get(root.toString(), "" + message.getDispatched().getId()));
            LOGGER.info(String.format("Driver %d processed order %d", getId(), message.getDispatched().getId()));
            return LocalDateTime.now();
        });
    }

    public void shutdown() {
        service.shutdown();
    }
}
