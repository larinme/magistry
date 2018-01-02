package ru.ifmo.entity;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {

    PLAINT_TEXT {
        @Override
        public char represent() {
            return 't';
        }
    },
    LINK {
        @Override
        public char represent() {
            return 'l';
        }
    },
    EMOTICON {
        @Override
        public char represent() {
            return 's';
        }
    },
    HASH_TAG {
        @Override
        public char represent() {
            return '#';
        }
    },
    DATE {
        @Override
        public char represent() {
            return 'd';
        }
    },
    QUOTE {
        @Override
        public char represent() {
            return 'q';
        }
    };

    public static final Map<Character, TokenType> PRESENTERS;

    static {
        PRESENTERS = new HashMap<>();
        for (TokenType type : values()) {
            PRESENTERS.put(type.represent(), type);
        }
    }

    public abstract char represent();
}
