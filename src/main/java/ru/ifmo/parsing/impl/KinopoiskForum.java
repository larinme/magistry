package ru.ifmo.parsing.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Pattern;

public class KinopoiskForum extends AbstractParser {

    private final String out;
    private static final Pattern COUNT_OF_PAGES_PATTERN = Pattern.compile("Страница \\d* из \\d*");

    public KinopoiskForum(String out) {
        this.out = out;
    }

    public void parse(String url) throws IOException {
        Document document = Jsoup.connect(url).get();

    }

    Pattern getCountOfPagesPattern() {
        return COUNT_OF_PAGES_PATTERN;
    }
}
