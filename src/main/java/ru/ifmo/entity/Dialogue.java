package ru.ifmo.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public List<Dialogue> getSubDialogues(int length) {
        List<Dialogue> dialogues = new ArrayList<>();
        if (size() <= length) {
            return Collections.singletonList(this);
        }
        for (int i = 0; i < messages.size() - length; i++) {
            List<Message> messages = this.messages.subList(i, i + length);
            dialogues.add(new Dialogue(messages));
        }
        return dialogues;
    }
}
