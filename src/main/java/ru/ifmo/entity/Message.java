package ru.ifmo.entity;

import java.util.*;

public class Message extends AbstractEntity {

    private final Topic topic;
    private final Author author;
    private final int orderNum;
    private final Date date;
    private Message reference;
    private List<Token> tokens = new ArrayList<>();
    private String text;
    private boolean isLeaf = true;
    private final String link;

    public Message(long id, Topic topic, Author author, String text, int orderNum, Date date, String link) {
        super(id);
        this.topic = topic;
        this.author = author;
        this.text = text;
        this.orderNum = orderNum;
        this.date = date;
        this.link = link;
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

    public boolean isLeaf() {
        return isLeaf;
    }

    private void setIsLeaf(boolean list) {
        isLeaf = list;
    }

    public void addToken(Token token){
        tokens.add(token);
    }

    public String getTokenStringPresenter(){
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token.getValue()).append(" ");
        }
        return builder.toString();
    }

    public String getLink() {
        return link;
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
