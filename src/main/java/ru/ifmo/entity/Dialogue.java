package ru.ifmo.entity;

import java.util.ArrayList;
import java.util.List;

public class Dialogue extends AbstractEntity {

    private List<Message> messages = new ArrayList<>();

    public Dialogue(long id) {
        super(id);
    }

    public void add(Message message){
        messages.add(message);
    }

    public boolean containsMessage(Message message){
        for (Message currMessage : messages) {
            if (currMessage.equals(message)) {
                return true;
            }
        }
        return false;
    }
}
