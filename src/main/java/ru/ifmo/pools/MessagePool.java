package ru.ifmo.pools;

import org.apache.commons.text.similarity.JaccardSimilarity;
import ru.ifmo.entity.Author;
import ru.ifmo.entity.Message;
import ru.ifmo.entity.Topic;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class MessagePool {

    private static MessagePool instance = new MessagePool();
    private Set<Message> pool = new HashSet<>();
    private Map<Topic, Message> startMessages = new HashMap<>();
    private long nextId = 0;
    public static final Equals<String> ARE_MESSAGES_TEXT_EQUAL = (src, object) ->
            new JaccardSimilarity().apply(src, object) > 0.8
            || object.contains(src.substring(0, 15));

    public static MessagePool getInstance() {
        if (instance == null) {
            instance = new MessagePool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public Message put(Topic topic, Author author, String text, int orderNum, Date date) {
        Message message = new Message(getNextId(), topic, author, text, orderNum, date);
        startMessages.putIfAbsent(topic, message);
        pool.add(message);
        return message;
    }

    public Message getMessageByText(String text, final Topic topic, Message defaultMessage) {
        for (Message message : pool) {
            String messageText = message.getText();
            if (message.getTopic().equals(topic) && ARE_MESSAGES_TEXT_EQUAL.areEquals(text, messageText)) {
                return message;
            }
        }
        return defaultMessage;
    }

    public Message getFirstMessage(Topic topic) {
        return startMessages.get(topic);
    }

    public Set<Message> getPool() {
        return pool;
    }

    public List<Message> getLeafMessages(){
        return pool.stream().filter(Message::isLeaf).collect(toList());
    }

}
