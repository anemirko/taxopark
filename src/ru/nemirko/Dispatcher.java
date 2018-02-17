package ru.nemirko;

import javax.xml.bind.JAXBException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Сущность диспетчер
 *
 * @author Alexey Nemirko
 */
public class Dispatcher {

    private AtomicInteger counter = new AtomicInteger(1);
    private Path root;
    private int maxDrivers;
    private Map<Integer,Future<LocalDateTime>> map = new ConcurrentHashMap<>();
    private Set<Driver> drivers;
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(Path root, int maxDrivers) {
        this.root = root;
        this.maxDrivers = maxDrivers;

        List<Path> driversPath = IntStream
                .rangeClosed(1, maxDrivers)
                .mapToObj(i -> Paths.get(root.toString(), Integer.toString(i)))
                .collect(Collectors.toList());

        Set<Driver> set = new LinkedHashSet<>();
        for (int i = 0; i < driversPath.size(); i++) {
            Util.prepareDirectory(driversPath.get(i)); //Создать или очистить директорию
            set.add(new Driver(i + 1, driversPath.get(i)));
        }
        drivers = Collections.unmodifiableSet(set);
    }

    private Driver getDriverById(Integer id) {
        Optional<Driver> opt = drivers.stream().filter(driver -> driver.getId().equals(id)).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        }
        LOGGER.log(Level.WARNING, String.format("Driver %d not found", id));
        return null;
    }

    public synchronized Integer processMessage(String xml) throws JAXBException {
        Message message = Util.messageFromXML(xml);
        Integer targetId = message.getTarget().getId();
        message.setDispatched(counter.getAndIncrement());
        map.put(message.getDispatched().getId(), getDriverById(targetId).addMessage(message));
        LOGGER.info(message + " add in queue driver "+ targetId);
        return message.getDispatched().getId();
    }
    public String getStatusMessageById(Integer dispatchedId) {
        if (map.get(dispatchedId).isDone()) {
            try {
                return String.format("Заказ %d выполнен %s", dispatchedId,
                        map.get(dispatchedId).get().format(DateTimeFormatter.ISO_DATE_TIME));
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        }
        return String.format("Заказ %d пока не исполнен", dispatchedId);
    }

    public void shutdown() {
        this.drivers.forEach(Driver::shutdown);
    }

    public static void main(String[] args) {
        Dispatcher dispatcher = new Dispatcher(Paths.get("/Users/anemirko/Documents/taxi/"), 10);
        new Random().ints(25, 1, dispatcher.maxDrivers + 1)
                .forEach(value -> {
                    Message message = Util.createMessage(value);
                    System.out.println(message);
                    try {
                        int id = dispatcher.processMessage(Util.messageToXML(message));
                        LOGGER.info("Generate message dispatched id = " + id);
                    } catch (JAXBException e) {
                        LOGGER.log(Level.WARNING, e.getMessage());
                    }
                });
        try {
            Thread.sleep(8000);
        } catch (Exception e) {}
        System.out.println(dispatcher.getStatusMessageById(1));
        System.out.println(dispatcher.getStatusMessageById(12));
        dispatcher.shutdown();
    }
}
