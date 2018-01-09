package ru.ifmo.entity;

import java.util.HashMap;
import java.util.Map;

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
}
