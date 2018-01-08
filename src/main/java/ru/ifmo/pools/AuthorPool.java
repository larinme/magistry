package ru.ifmo.pools;

import ru.ifmo.entity.Author;

import java.util.HashSet;
import java.util.Set;

public class AuthorPool {

    private static AuthorPool instance = new AuthorPool();
    private Set<Author> pool = new HashSet<>();
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
            pool.add(author);
        }
        return author;
    }

    private Author getAuthorByName(String name) {
        for (Author author : pool) {
            if (author.getName().equals(name)) {
                return author;
            }
        }
        return null;
    }

}
