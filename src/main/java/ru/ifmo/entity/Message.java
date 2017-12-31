package ru.ifmo.entity;

import java.util.Date;

public class Message extends AbstractEntity {

    private final Topic topic;
    private final Author author;
    private final Message message;
    private final String text;
    private final int orderNum;
    private final Date date;

    public Message(long id, Topic topic, Author author, Message message, String text, int orderNum, Date date) {
        super(id);
        this.topic = topic;
        this.author = author;
        this.message = message;
        this.text = text;
        this.orderNum = orderNum;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id == message.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
