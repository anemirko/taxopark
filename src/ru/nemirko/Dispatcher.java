package ru.nemirko;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
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
    //Константа путь к родительской директории где будут создаваться рабочие подкаталоги для водителей
    //Необходимо отредактировать
    private static final Path ROOT = Paths.get("/Users/anemirko/Documents/taxi/");
    //Количество водителей
    private static final int MAX_DRIVERS = 10;
    //Множество водителей
    private static final Set<Driver> DRIVERS;
    private static final ExecutorService DRIVERS_SERVICE = Executors.newFixedThreadPool(MAX_DRIVERS);
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    static {
        //Подготовка директорий и водителей
        final List<Path> driversPath = IntStream
                .rangeClosed(1, MAX_DRIVERS)
                .mapToObj(i -> Paths.get(ROOT.toString(), Integer.toString(i)))
                .collect(Collectors.toList());
        Set<Driver> set = new LinkedHashSet<>();

        for (int i = 0; i < driversPath.size(); i++) {
            Util.prepareDirectory(driversPath.get(i)); //Создать или очистить директорию
            set.add(new Driver(i + 1, driversPath.get(i)));
        }
        DRIVERS = Collections.unmodifiableSet(set);
    }

    public synchronized Integer receive(String xml) {
        //-1 не добавилось сообщение в очередь к водителю, надо думать
        int id = -1;
        try {
            Message message = Message.fromXML(xml);
            message.setDaspatched(counter.getAndIncrement());
            id = message.getDispatched().getId();
            DRIVERS
                    .stream()
                    .filter(driver -> message.getTarget().getId().equals(driver.getId()))
                    .findFirst()
                    .ifPresent(driver -> {
                        if (driver.addMessage(message)) {
                            DRIVERS_SERVICE.execute(driver);
                        }
                    });
        } catch (JAXBException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return id;
    }

    public static void main(String[] args) throws IOException {
        Dispatcher dispatcher = new Dispatcher();
        List<String> DATA = Arrays.asList(" ", " ", " ");
        new Random().ints(50, 1, MAX_DRIVERS + 1)
                .forEach(value -> {
                    Message message = new Message(value, DATA); //
                    try {
                        int id = dispatcher.receive(message.toXML());
                        LOGGER.info("Generate message dispatched id =" + id);
                    } catch (JAXBException e) {
                        LOGGER.log(Level.WARNING, e.getMessage());
                    }
                });
        DRIVERS_SERVICE.shutdown();
    }
}
