package ru.ifmo.utils.impl;

import ru.ifmo.entity.utils.ComparableDocument;
import ru.ifmo.utils.DocumentDownloader;
import ru.ifmo.utils.DocumentDownloadingThread;

import java.util.*;

public class DocumentDownloaderImpl implements DocumentDownloader {

    private static volatile DocumentDownloaderImpl instance;

    private DocumentDownloaderImpl() {
    }

    public static DocumentDownloaderImpl getInstance() {
        if (instance == null) {
            synchronized (DocumentDownloaderImpl.class) {
                if (instance == null) {
                    instance = new DocumentDownloaderImpl();
                }
            }
        }

        return instance;
    }

    @Override
    public SortedSet<ComparableDocument> getDocuments(String url, String pageParameter, int pageCount) {
        SortedSet<ComparableDocument> documents = new TreeSet<>();
        Collection<DocumentDownloadingThread> activeThreads = new ArrayList<>();
        List<DocumentDownloadingThread> threads = new ArrayList<>();
        for (int i = 0; i < COUNT_OF_THREADS; i++) {
            int range = (pageCount / COUNT_OF_THREADS) + 1;
            int startPage = (range * i) + 1;
            int endPage = Math.min(pageCount, (range * (i + 1)));
            threads.add(new DocumentDownloadingThread(url + "&" + pageParameter, startPage, endPage));
        }

        startDownloadThreads(activeThreads, threads);
        waitPageDownloadFinish(activeThreads, threads);

        for (DocumentDownloadingThread thread : threads) {
            SortedSet<ComparableDocument> threadDocuments = thread.getDocuments();
            documents.addAll(threadDocuments);
        }
        return documents;
    }

    private void waitPageDownloadFinish(Collection<DocumentDownloadingThread> activeThreads, List<DocumentDownloadingThread> threads) {
        while (!activeThreads.isEmpty()) {
            for (DocumentDownloadingThread thread : threads) {
                if (thread.getState().equals(Thread.State.RUNNABLE)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    activeThreads.remove(thread);
                }
            }
        }
    }

    private void startDownloadThreads(Collection<DocumentDownloadingThread> activeThreads, List<DocumentDownloadingThread> threads) {
        for (DocumentDownloadingThread thread : threads) {
            activeThreads.add(thread);
            thread.start();
        }
    }
}
