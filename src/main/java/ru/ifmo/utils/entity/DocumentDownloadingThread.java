package ru.ifmo.utils.entity;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.ifmo.entity.utils.ComparableDocument;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

public class DocumentDownloadingThread extends Thread {

    private final String url;
    private final int startPageNum;
    private final int endPageNum;
    private SortedSet<ComparableDocument> documents = new TreeSet<>();
    private static final Logger log = Logger.getLogger(DocumentDownloadingThread.class);


    public DocumentDownloadingThread(String url, int startPageNum, int endPageNum) {
        this.url = url;
        this.startPageNum = startPageNum;
        this.endPageNum = endPageNum;
    }

    @Override
    public void run() {
        Document document;
        for (int currentPage = startPageNum; currentPage <= endPageNum; currentPage++) {
            try {
                document = Jsoup.connect(url + "=" + currentPage).get();
                log.debug("Page #" + currentPage + " downloaded");
            } catch (IOException e) {
                throw new RuntimeException("Document cannot be loaded...", e);
            }
            ComparableDocument comparableDocument = new ComparableDocument(document, currentPage);
            documents.add(comparableDocument);
        }
    }

    public SortedSet<ComparableDocument> getDocuments() {
        return documents;
    }
}
