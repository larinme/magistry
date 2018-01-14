package ru.ifmo.utils.impl;

import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;
import ru.ifmo.utils.DialogueBuilder;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class DialogueBuilderImpl implements DialogueBuilder {

    @Override
    public Dialogue build(Message message) {
        Set<Message> dialogues = new TreeSet<>(Comparator.comparingInt(Message::getOrderNum));
        Message reference = message;
        while (reference != null) {
            dialogues.add(reference);
            if (reference != reference.getReference()) {
                reference = reference.getReference();
            } else {
                break;
            }
        }
        return new Dialogue(dialogues);
    }
}
