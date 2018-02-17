package ru.nemirko;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
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

    public void setDispatched(int dispatched) {
        this.dispatched = new Id(dispatched);
    }

    public void setDispatched(Id dispatched) {
        this.dispatched = dispatched;
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
        Integer targetId = target != null ? target.getId() : null;
        Integer dispatchedId = dispatched != null ? dispatched.getId() : null;
        return "Message{" +
                "target id=" + targetId +
                ", dispatched id=" + dispatchedId +
                ", data=" + data +
                '}';
    }
}
