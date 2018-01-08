package ru.ifmo.pools;

import ru.ifmo.entity.Source;
import ru.ifmo.entity.Topic;

import java.util.HashSet;
import java.util.Set;

public class TopicPool {

    private static TopicPool instance = new TopicPool();
    private Set<Topic> pool = new HashSet<>();
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
            pool.add(topic);
        }
        return topic;
    }

    private Topic getTopicByName(String title, Source source) {
        for (Topic topic : pool) {
            if (topic.getTitle().equals(title)
                    && topic.getSource().equals(source)) {
                return topic;
            }
        }
        return null;
    }

}
