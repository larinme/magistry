package ru.ifmo.entity;

import java.util.Date;

public class Message extends AbstractEntity {

    private final Topic topic;
    private final Author author;
    private final int orderNum;
    private final Date date;
    private Message reference;
    private String text;
    private boolean isLeaf = true;

    public Message(long id, Topic topic, Author author, String text, int orderNum, Date date) {
        super(id);
        this.topic = topic;
        this.author = author;
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

    public Topic getTopic() {
        return topic;
    }

    public Author getAuthor() {
        return author;
    }

    public Message getReference() {
        return reference;
    }

    public void setReference(Message reference) {
        this.reference = reference;
        setIsLeaf(true);
        if (reference != null) {
            reference.setIsLeaf(false);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public Date getDate() {
        return date;
    }

    public int getDialogueLength() {
        Message currentMessage = this;
        int dialogueLength = 0;
        while (currentMessage != null) {
            dialogueLength++;
            currentMessage = currentMessage.getReference();
        }
        return dialogueLength;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    private void setIsLeaf(boolean list) {
        isLeaf = list;
    }

    @Override
    public String toString() {
        return "Message{" +
                "topic=" + topic +
                ", author=" + author +
                ", text='" + text + '\'' +
                ", orderNum=" + orderNum +
                ", isLeaf=" + isLeaf +
                ", id=" + id +
                '}';
    }
}
