package ru.ifmo.parsing.impl;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.ifmo.entity.TokenType;
import ru.ifmo.utils.DialogueWriter;
import ru.ifmo.utils.DocumentDownloader;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KinopoiskForumParser extends AbstractParser {

    private static final String TITLE_QUERY = ".navbar > strong";
    private static final String DATE_QUERY = "td.thead";
    private static final String USERNAME_QUERY = "a.bigusername";
    private static final String POST_QUERY = "post_message_";
    private static final Pattern COUNT_OF_PAGES_PATTERN = Pattern.compile("Страница \\d* из \\d*");
    private static final DateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.ENGLISH);
    private static final String PAGE_NUMBER_PARAMETER = "page";
    private static final String POSTS_CONTAINER = "posts";
    private static final String SOURCE_NAME = "Kinopoisk";
    private static final String THEMA_NAME = "Кино";
    private Map<TokenType, Function<String, String>> TOKEN_TYPE_PROCESSORS = new HashMap<>();

    {
        for (TokenType type : TokenType.values()) {
            if (TokenType.QUOTE.equals(type)) {
                TOKEN_TYPE_PROCESSORS.put(type, (value) -> {
                    Matcher matcher = HTML_PATTERN.matcher(value);
                    StringBuilder builder = new StringBuilder();
                    while (matcher.find()) {
                        builder.append(matcher.group()).append("!");
                    }
                    String[] tags = builder.toString().split("!");
                    for (String tag : tags) {
                        value = value.replaceAll(tag, "");
                    }
                    String[] split = value.split(" ");
                    int numberOfElements = 0;
                    builder = new StringBuilder();
                    for (String s : split) {
                        if (!s.equals(" ") && !s.equals("") && !s.startsWith("$l")) {
                            if (numberOfElements++ >= 4) {
                                builder.append(s).append(" ");
                            }
                        }
                    }
                    return builder.toString();
                });
            } else {
                TOKEN_TYPE_PROCESSORS.put(type, DEFAULT_TOKEN_TYPE_PROCESSOR);
            }
        }
    }

    @Inject
    public KinopoiskForumParser(DocumentDownloader documentDownloader, DialogueWriter writer) {
        super(documentDownloader, writer);
    }

    @Override
    public Pattern getCountOfPagesPattern() {
        return COUNT_OF_PAGES_PATTERN;
    }

    @Override
    String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public String getThema() {
        return THEMA_NAME;
    }

    @Override
    public String getTitleQuery() {
        return TITLE_QUERY;
    }

    @Override
    public String getAuthorQuery() {
        return USERNAME_QUERY;
    }

    @Override
    public Map<TokenType, Function<String, String>> getTokenTypeProcessor() {
        return TOKEN_TYPE_PROCESSORS;
    }

    @Override
    public DateFormat getDateFormat() {
        return format;
    }

    @Override
    public String getDateQuery() {
        return DATE_QUERY;
    }

    @Override
    public String getPostQuery() {
        return POST_QUERY;
    }

    @Override
    public String getPageNumberParameter() {
        return PAGE_NUMBER_PARAMETER;
    }

    @Override
    public Elements getPosts(Document document) {
        return document.getElementById(POSTS_CONTAINER).getElementsByClass("tborder");
    }
}
