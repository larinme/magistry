package ru.ifmo.utils.impl;

import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;
import ru.ifmo.utils.DialogueBuilder;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class DialogueBuilderImpl implements DialogueBuilder {

    private static DialogueBuilderImpl instance;

    private DialogueBuilderImpl(){}

    public static DialogueBuilderImpl getInstance() {
        if (instance == null){
            synchronized (DialogueBuilderImpl.class){
                if (instance == null) {
                    instance = new DialogueBuilderImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public Dialogue build(Message message) {
        Set<Message> dialogues = new TreeSet<>(Comparator.comparingInt(Message::getOrderNum));
        Message reference = message;
        while (reference != null) {
            dialogues.add(reference);
            reference = reference.getReference();
        }
        return new Dialogue(dialogues);
    }
}
