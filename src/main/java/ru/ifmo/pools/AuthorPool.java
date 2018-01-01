package ru.ifmo.pools;

import ru.ifmo.entity.Author;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AuthorPool {

    private static AuthorPool instance = new AuthorPool();
    private Map<Long, Author> pool = new HashMap<>();
    private long nextId = 0;

    public static AuthorPool getInstance() {
        if (instance == null) {
            instance = new AuthorPool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public Author putIfNotExists(String name, String city) {
        Author author = getAuthorByName(name);
        if (author == null) {
            author = new Author(getNextId(), name, city);
            put(author);
        }
        return author;
    }

    private Author getAuthorByName(String name) {
        Collection<Author> values = pool.values();
        for (Author author : values) {
            if (author.getName().equals(name)) {
                return author;
            }
        }
        return null;
    }

    private void put(Author author) {
        long id = author.getId();
        pool.put(id, author);
    }
}
