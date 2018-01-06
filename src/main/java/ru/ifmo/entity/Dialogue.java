package ru.ifmo.entity;

import ru.ifmo.pools.Contains;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Dialogue extends AbstractEntity {

    private List<Message> messages = new ArrayList<>();
    public Dialogue(long id) {
        super(id);
    }

    public void add(Message message){
        messages.add(message);
    }

    public boolean containsMessage(Message message){
        return containsMessage(message, Object::equals);
    }

    public boolean containsMessage(Message message, Contains contains){
        return messages.stream().anyMatch((msg) -> contains.areEquals(msg, message));
    }

    public List<Message> getMessages() {
        return messages;
    }
}
