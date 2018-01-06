package ru.ifmo.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public enum TokenType {

    PLAINT_TEXT {
        @Override
        public char code() {
            return 't';
        }
    },
    LINK {
        @Override
        public char code() {
            return 'l';
        }
    },
    EMOTICON {
        @Override
        public char code() {
            return 's';
        }
    },
    HASH_TAG {
        @Override
        public char code() {
            return '#';
        }
    },
    DATE {
        @Override
        public char code() {
            return 'd';
        }
    },
    QUOTE {
        @Override
        public char code() {
            return 'q';
        }
    };

    public static final Map<Character, TokenType> PRESENTERS;

    static {
        PRESENTERS = new HashMap<>();
        for (TokenType type : values()) {
            PRESENTERS.put(type.code(), type);
        }
    }

    public abstract char code();

    public String perform(String value, Function<String, String> function) {
        return function.apply(value);
    }
}
