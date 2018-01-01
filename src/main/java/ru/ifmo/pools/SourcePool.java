package ru.ifmo.pools;

import ru.ifmo.entity.Source;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SourcePool {

    private static SourcePool instance = new SourcePool();
    private Map<Long, Source> pool = new HashMap<>();
    private long nextId = 0;

    private long getNextId() {
        return ++nextId;
    }

    public Source putIfNotExists(String name, String url){
        Source source = getSourceByName(name);
        if (source == null){
            source = new Source(getNextId(), name, url);
            put(source);
        }
        return source;
    }

    private Source getSourceByName(String name){
        Collection<Source> values = pool.values();
        for (Source source : values) {
            if (source.getName().equals(name)){
                return source;
            }
        }
        return null;
    }

    private void put(Source source){
        long id = source.getId();
        pool.put(id, source);
    }

    public static SourcePool getInstance() {
        if (instance == null) {
            instance = new SourcePool();
        }
        return instance;
    }
}
