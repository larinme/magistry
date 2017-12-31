package ru.ifmo.entity;

public class Token extends AbstractEntity{

    private final TokenType tokenType;
    private final String value;
    private final long messageID;
    private final short orderNumber;

    public Token(long id, TokenType tokenType, String value, long messageID, short orderNumber) {
        super(id);
        this.tokenType = tokenType;
        this.value = value;
        this.messageID = messageID;
        this.orderNumber = orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        return id == token.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
