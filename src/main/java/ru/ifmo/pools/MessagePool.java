package ru.ifmo.pools;

import org.apache.commons.text.similarity.JaccardSimilarity;
import ru.ifmo.entity.Author;
import ru.ifmo.entity.Message;
import ru.ifmo.entity.Source;
import ru.ifmo.entity.Topic;

import java.util.*;

public class MessagePool {

    private static MessagePool instance = new MessagePool();
    private Set<Message> pool = new HashSet<>();
    private Map<Topic, Message> startMessages = new HashMap<>();
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
        startMessages.putIfAbsent(topic, message);
        pool.add(message);
        return message;
    }

    public Message getMessageByText(String text, final Topic topic, Message defaultMessage) {
        for (Message message : pool) {
            if (message.getTopic().equals(topic) &&
                    (new JaccardSimilarity().apply(text, message.getText()) > 0.7)
                    || message.getText().contains(text.substring(0, 15))) {
                return message;
            }
        }
        return defaultMessage;
        /*Optional<Message> message = pool.stream()
                .filter((msg) -> msg.getTopic().equals(topic) &&
                        ( new JaccardSimilarity().apply(text, msg.getText()) > 0.7)
                        || text.substring(0, 15).startsWith(msg.getText())
                )
                .findFirst();
        return message.orElse(defaultMessage);*/
    }

    public Message getFirstMessage(Topic topic) {
        return startMessages.get(topic);
    }
}
