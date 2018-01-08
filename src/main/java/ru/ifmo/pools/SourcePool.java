package ru.ifmo.pools;

import ru.ifmo.entity.Source;

import java.util.HashSet;
import java.util.Set;

public class SourcePool {

    private static SourcePool instance = new SourcePool();
    private Set<Source> pool = new HashSet<>();
    private long nextId = 0;

    public static SourcePool getInstance() {
        if (instance == null) {
            instance = new SourcePool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public Source putIfNotExists(String name, String url) {
        Source source = getSourceByName(name);
        if (source == null) {
            source = new Source(getNextId(), name, url);
            pool.add(source);
        }
        return source;
    }

    private Source getSourceByName(String name) {
        for (Source source : pool) {
            if (source.getName().equals(name)) {
                return source;
            }
        }
        return null;
    }

}
