package ru.nemirko;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Сущность водитель
 *
 * @author Alexey Nemirko
 */

public class Driver implements Runnable {

    private Integer id;
    private Path root;
    private final Queue<Message> queue = new ConcurrentLinkedQueue<>();
    public static final int MONKEY_JOB = 3;  //3 секунды
    private volatile boolean isBusy = false;
    private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());

    public Driver(Integer id, Path root) {
        if (Files.isDirectory(root)) {
            this.id = id;
            this.root = root;
        } else {
            throw new IllegalArgumentException("You must specify the correct path to working directory for driver " + id);
        }
    }

    public Integer getId() {
        return id;
    }

    public boolean addMessage(Message message) {
        LOGGER.info("adding message = " + message);
        return queue.add(message);
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void run() {
        while (isBusy()) {
        }
        setBusy(true);
        LOGGER.info(String.format("Driver %d started the next order", id));
        try {
            Message message = queue.poll();
            if (message != null) {
                message.toFile(Paths.get(root.toString(), "" + message.getDispatched().getId()));
                TimeUnit.SECONDS.sleep(MONKEY_JOB);
            } else {
                LOGGER.info(String.format("Queue driver %d is empty", id));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            setBusy(false);
        }

    }
}
