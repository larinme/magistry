package ru.ifmo.entity;

public class Token extends AbstractEntity {

    private final TokenType tokenType;
    private final String value;
    private final Message message;
    private int orderNumber;

    public Token(long id, TokenType tokenType, String value, Message message, int orderNumber) {
        super(id);
        this.tokenType = tokenType;
        this.value = value;
        this.message = message;
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

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getValue() {
        return value;
    }

    public Message getMessage() {
        return message;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
