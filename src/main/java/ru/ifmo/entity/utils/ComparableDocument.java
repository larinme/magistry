package ru.ifmo.entity.utils;

import org.jsoup.nodes.Document;

public class ComparableDocument implements Comparable<ComparableDocument> {

    private final Document document;
    private final int orderNumber;

    public ComparableDocument(Document document, int orderNumber) {
        this.document = document;
        this.orderNumber = orderNumber;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    @Override
    public int compareTo(ComparableDocument other) {
        return Integer.compare(orderNumber, other.getOrderNumber());
    }

    public Document getDocument() {
        return document;
    }
}
