package ru.nemirko;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Вот такая вот сущность лишь бы xml был нормальный
 *
 * @author Alexey Nemirko
 */
@XmlType
public class Id {
    Integer id;

    public Id() {
    }

    public Id(Integer id) {
        this.id = id;
    }

    @XmlAttribute
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Id{" +
                "id=" + id +
                '}';
    }
}
