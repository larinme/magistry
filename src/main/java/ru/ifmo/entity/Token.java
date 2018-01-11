package ru.ifmo.entity;

public class Token extends AbstractEntity implements Comparable<Token> {

    private final TokenType tokenType;
    private final Message message;
    private int orderNumber;
    private String value;

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

    public void setValue(String value) {
        this.value = value;
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

    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", message=" + message +
                ", orderNumber=" + orderNumber +
                ", value='" + value + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public int compareTo(Token o) {
        return Integer.compare(message.getOrderNum(), o.getMessage().getOrderNum());
    }
}
