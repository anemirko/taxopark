package ru.nemirko;

import javax.xml.bind.JAXBException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
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
    private int maxDrivers;
    private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private Map<Integer,Future<LocalDateTime>> map = new ConcurrentHashMap<>();
    private Set<Driver> drivers;
    private final ExecutorService service = Executors.newFixedThreadPool(1);
    private volatile boolean stop = false;
    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());

    public Dispatcher(Path root, int maxDrivers) {
        this.maxDrivers = maxDrivers;
        drivers = IntStream
                .rangeClosed(1, maxDrivers)
                .mapToObj(i -> {
                    Path path = Paths.get(root.toString(), Integer.toString(i));
                    Util.prepareDirectory(path); //Создать или очистить директорию
                    return new Driver(i, path);
                }).collect(Collectors.toSet());
        service.execute(() -> {
            try {
                while (!stop) {
                    Message message = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        Integer targetId = message.getTarget().getId();
                        Driver driver = getDriverById(targetId);
                        if (driver != null) {
                            map.put(message.getDispatched().getId(), driver.addMessage(message));
                            LOGGER.info(message + " add in queue driver " + targetId);
                        } else {
                            LOGGER.log(Level.WARNING,message + " not found driver with id=" + targetId);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Прервана работа диспетчера");
            }
        });
    }

    private Driver getDriverById(Integer id) {
        Optional<Driver> opt = drivers.stream().filter(driver -> driver.getId().equals(id)).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        }
        LOGGER.log(Level.WARNING, String.format("Водитель %d не найден", id));
        return null;
    }

    public Integer processMessage(String xml) throws JAXBException {
        Message message = Util.messageFromXML(xml);
        message.setDispatched(counter.getAndIncrement());
        queue.add(message);
        LOGGER.info(message + " add in dispatcher's queue");
        return message.getDispatched().getId();
    }

    public String getStatusMessageById(Integer dispatchedId) {
        if (map.containsKey(dispatchedId)) {
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
        return String.format("Заказ %d пока не обрабатывался", dispatchedId);
    }

    public void shutdown() {
        stop = true;
        service.shutdown();
        drivers.forEach(Driver::shutdown);
    }

    public static void main(String[] args) throws InterruptedException{
        Dispatcher dispatcher = new Dispatcher(Paths.get("/Users/anemirko/Documents/taxi/"), 10);
        //Загрузим 1000 сообщений 4-мя потоками по 250 сообщений
        List<Runnable> run = IntStream
                .rangeClosed(1, 4)
                .mapToObj((IntFunction<Runnable>) value -> () -> new Random().ints(250, 1, dispatcher.maxDrivers + 1)
                        .mapToObj(Util::createMessage)
                        .forEach(message -> {
                            try {
                                int id = dispatcher.processMessage(Util.messageToXML(message));
                                LOGGER.info("1 - Generate message dispatched id = " + id);
                            } catch (JAXBException e) {
                                LOGGER.log(Level.WARNING, e.getMessage());
                            }
                        })).collect(Collectors.toList());
        ExecutorService load = Executors.newFixedThreadPool(3);
        run.forEach(load::submit);
        load.shutdown();
        //Проверка получения статуса выполнения заказа
        TimeUnit.SECONDS.sleep(8);
        System.out.println(dispatcher.getStatusMessageById(1));
        System.out.println(dispatcher.getStatusMessageById(546));
        dispatcher.shutdown();
    }
}
