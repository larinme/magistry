package ru.ifmo.pools;

import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;

import java.util.*;

public class DialoguePool {

    private static DialoguePool instance = new DialoguePool();
    private Set<Dialogue> pool = new HashSet<>();
    private long nextId = 0;

    public static DialoguePool getInstance() {
        if (instance == null) {
            instance = new DialoguePool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public void put(Message message){
        pool.add(new Dialogue(getNextId()));
        Collection<Dialogue> dialogues = findDialoguesWithMessage(message);

        for (Dialogue dialogue : dialogues) {
            dialogue.add(message);
        }
    }

    private Collection<Dialogue> findDialoguesWithMessage(Message message) {
        Collection<Dialogue> dialogues = new ArrayList<>();
        for (Dialogue value : pool) {
            if (value.containsMessage(message)){
                dialogues.add(value);
            }
        }
        return dialogues;
    }

}
