package ru.ifmo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Dialogue {

    private List<Message> messages = new ArrayList<>();

    public Dialogue(Collection<Message> dialogueMessages) {
        messages.clear();
        messages.addAll(dialogueMessages);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int size() {
        return messages.size();
    }
}
