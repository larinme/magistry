package ru.ifmo.pools;

import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;

import java.util.*;
import java.util.stream.Collectors;

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

    public void put(Message message, Contains contains){
        List<Dialogue> dialogues = pool.stream()
                .filter((dialogue -> dialogue.containsMessage(message, contains)))
                .collect(Collectors.toList());
        if (dialogues.size() > 0) {
            dialogues.forEach((dialogue -> dialogue.add(message)));
        } else {
            put(message);
        }
    }

    public Dialogue put(Message message) {
        Dialogue dialogue = new Dialogue(getNextId());
        dialogue.add(message);
        pool.add(dialogue);
        return dialogue;
    }

    public Set<Dialogue> getPool() {
        return pool;
    }
}
