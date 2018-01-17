package ru.ifmo.utils.impl;

import ru.ifmo.entity.utils.ComparableDocument;
import ru.ifmo.utils.DocumentDownloader;
import ru.ifmo.utils.ThreadRunner;
import ru.ifmo.utils.entity.DocumentDownloadingThread;

import javax.inject.Inject;
import java.util.*;

public class DocumentDownloaderImpl implements DocumentDownloader {

    private ThreadRunner<DocumentDownloadingThread> threadRunner;

    @Inject
    public DocumentDownloaderImpl(ThreadRunner<DocumentDownloadingThread> threadRunner) {
        this.threadRunner = threadRunner;
    }

    @Override
    public SortedSet<ComparableDocument> getDocuments(String url, String pageParameter, int pageCount) {
        List<DocumentDownloadingThread> threads = new ArrayList<>();
        int range = (pageCount / COUNT_OF_THREADS) + 1;
        for (int i = 0; i < COUNT_OF_THREADS; i++) {
            int startPage = (range * i) + 1;
            int endPage = Math.min(pageCount, (range * (i + 1)));
            threads.add(new DocumentDownloadingThread(url + "&" + pageParameter, startPage, endPage));
        }

        threadRunner.start(threads);

        SortedSet<ComparableDocument> documents = new TreeSet<>();
        for (DocumentDownloadingThread thread : threads) {
            SortedSet<ComparableDocument> threadDocuments = thread.getDocuments();
            documents.addAll(threadDocuments);
        }
        return documents;
    }

}
