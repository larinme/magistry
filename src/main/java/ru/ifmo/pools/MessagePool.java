package ru.ifmo.pools;

import ru.ifmo.entity.Author;
import ru.ifmo.entity.Message;
import ru.ifmo.entity.Topic;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessagePool {

    private static MessagePool instance = new MessagePool();
    private Map<Long, Message> pool = new HashMap<>();
    private long nextId = 0;

    public static MessagePool getInstance() {
        if (instance == null) {
            instance = new MessagePool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public Message put(Topic topic, Author author, Message reference, String text, int orderNum, Date date) {
        Message message = new Message(getNextId(), topic, author, reference, text, orderNum, date);
        pool.put(message.getId(), message);
        return message;
    }
}
