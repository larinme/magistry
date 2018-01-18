package ru.ifmo.pools;

import org.apache.commons.text.similarity.JaccardSimilarity;
import ru.ifmo.entity.*;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class MessagePool {

    private static final Equals<String> ARE_MESSAGES_TEXT_EQUAL = (src, object) ->
            new JaccardSimilarity().apply(src, object) > 0.9
                    || object.contains(src.substring(0, Math.min(src.length(), 15)));
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

    public Message put(Topic topic, Author author, String text, int orderNum, Date date, String link) {
        Message message = new Message(getNextId(), topic, author, text, orderNum, date, link);
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

    public List<Message> getLeafMessages() {
        return pool.stream()
                .filter(Message::isLeaf)
                .sorted(Comparator.comparingInt(Message::getOrderNum))
                .collect(toList());
    }

    public int clear(final int range) {
        List<Message> messages = pool.stream()
                .filter(
                        msg -> msg.getOrderNum() > 1
                                && msg.getOrderNum() < range
                                && msg.isLeaf()
                                && msg.getReference() == null)
                .collect(toList());
        TokenPool.getInstance().remove(messages);
        pool.removeAll(messages);
        return pool.size();
    }

    public  void remove(Dialogue dialogue){
        pool.removeAll(dialogue.getMessages());
        TokenPool.getInstance().remove(dialogue.getMessages());
    }
}
