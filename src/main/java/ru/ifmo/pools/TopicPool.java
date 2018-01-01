package ru.ifmo.pools;

import ru.ifmo.entity.Source;
import ru.ifmo.entity.Topic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TopicPool {

    private static TopicPool instance = new TopicPool();
    private Map<Long, Topic> pool = new HashMap<>();
    private long nextId = 0;

    public static TopicPool getInstance() {
        if (instance == null) {
            instance = new TopicPool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public Topic putIfNotExists(Source source, String path, String thema, String title) {
        Topic topic = getTopicByName(title, source);
        if (topic == null) {
            topic = new Topic(getNextId(), source, path, thema, title);
            put(topic);
        }
        return topic;
    }

    private Topic getTopicByName(String title, Source source) {
        Collection<Topic> values = pool.values();
        for (Topic topic : values) {
            if (topic.getTitle().equals(title)
                    && topic.getSource().equals(source)) {
                return topic;
            }
        }
        return null;
    }

    private void put(Topic topic) {
        long id = topic.getId();
        pool.put(id, topic);
    }
}
