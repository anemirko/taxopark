package ru.nemirko;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сущность информационное сообщение
 *
 * @author Alexey Nemirko
 */
@XmlRootElement
@XmlType(propOrder = {"target", "dispatched", "data"})
public class Message {
    private static final Logger LOGGER = Logger.getLogger(Message.class.getName());

    private Id target;
    private Id dispatched;
    private List<String> data;


    @XmlElement(name = "target")
    public Id getTarget() {
        return target;
    }

    public void setTarget(Id target) {
        this.target = target;
    }

    public void setTarget(int target) {
        this.target = new Id(target);
    }

    @XmlElement(name = "dispatched")
    public Id getDispatched() {
        return dispatched;
    }

    public void setDaspatched(int dispatched) {
        this.dispatched = new Id(dispatched);
    }

    public void setDispatched(Id daspatched) {
        this.dispatched = daspatched;
    }

    @XmlElementWrapper(name = "sometags")
    @XmlElement
    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public Message() {
    }

    public Message(Integer target, List<String> data) {
        this.target = new Id(target);
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "target=" + target +
                ", daspatched=" + dispatched +
                ", data=" + data +
                '}';
    }
    //Возможно методы toXML, toFile, fromXML надо вынести в утильный класс
    public String toXML() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Message.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        marshaller.marshal(this, sw);
        return sw.toString();
    }

    public boolean toFile(Path path) {
        try {
            Files.write(path, toXML().getBytes(), StandardOpenOption.CREATE_NEW);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return false;
        /*try {
            FileWriter output = new FileWriter(path.toFile());
            output.write(toXML());
            output.close();
            LOGGER.info("Processed message = " + this);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return false;*/
    }

    public static Message fromXML(String xml) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Message.class);
        Unmarshaller um = jc.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        return (Message) um.unmarshal(reader);
    }
}
