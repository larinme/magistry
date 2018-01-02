package ru.ifmo.pools;

import ru.ifmo.entity.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TokenPool {

    private static TokenPool instance = new TokenPool();
    private Map<Long, Token> pool = new HashMap<>();
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
            put(token);
        }
        return token;
    }


    private Token getTopicByName(String value, Message message) {
        Collection<Token> values = pool.values();
        for (Token token : values) {
            if (token.getValue().equals(value)
                    && token.getMessage().equals(message)) {
                return token;
            }
        }
        return null;
    }

    private void put(Token token) {
        long id = token.getId();
        pool.put(id, token);
    }
}
