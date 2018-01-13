package ru.ifmo.utils;

import ru.ifmo.entity.utils.ComparableDocument;

import java.util.SortedSet;

public interface DocumentDownloader {

    int COUNT_OF_THREADS = 4;

    SortedSet<ComparableDocument> getDocuments(String url, String pageParameter, int pageCount);
}
