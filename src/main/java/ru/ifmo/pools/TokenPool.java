package ru.ifmo.pools;

import ru.ifmo.entity.*;

import java.util.*;

public class TokenPool {

    private static TokenPool instance = new TokenPool();
    private Set<Token> pool = new HashSet<>();
    private long nextId = 0;

    public static TokenPool getInstance() {
        if (instance == null) {
            instance = new TokenPool();
        }
        return instance;
    }

    private long getNextId() {
        return ++nextId;
    }

    public Token putIfNotExists(TokenType type, String value, Message message, int orderNum) {
        Token token = getTopicByName(value, message);
        if (token == null) {
            token = new Token(getNextId(), type, value, message, orderNum);
            pool.add(token);
        }
        return token;
    }


    private Token getTopicByName(String value, Message message) {
        for (Token token : pool) {
            if (token.getValue().equals(value)
                    && token.getMessage().equals(message)) {
                return token;
            }
        }
        return null;
    }

}
