package ru.ifmo.pools;

import ru.ifmo.entity.Author;
import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DialoguePool {

    private static DialoguePool instance = new DialoguePool();
    private Map<Long, Dialogue> pool = new HashMap<>();
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
        put(new Dialogue(getNextId()));
        Collection<Dialogue> dialogues = findDialoguesWithMessage(message);

        for (Dialogue dialogue : dialogues) {
            dialogue.add(message);
        }
    }

    private Collection<Dialogue> findDialoguesWithMessage(Message message) {
        Collection<Dialogue> dialogues = new ArrayList<>();
        for (Dialogue value : pool.values()) {
            if (value.containsMessage(message)){
                dialogues.add(value);
            }
        }
        return dialogues;
    }

    private void put(Dialogue dialogue) {
        long id = dialogue.getId();
        pool.put(id, dialogue);
    }
}
