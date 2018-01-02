package ru.ifmo.parsing.impl;

import com.google.common.collect.ImmutableMap;
import org.jsoup.nodes.Document;
import ru.ifmo.entity.TokenType;
import ru.ifmo.parsing.Parser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractParser implements Parser {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(\\B(#[a-zA-Z]+\\b)(?!;))");
    private static final Pattern LINK_PATTERN = Pattern.compile("((?i)<a([^>]+)>(.+?)</a>)");
    protected static final Map<TokenType, Pattern> PATTERNS = ImmutableMap.<TokenType, Pattern>builder()
            .put(TokenType.HASH_TAG, HASHTAG_PATTERN)
            .put(TokenType.LINK, LINK_PATTERN)
            .build();
    abstract Pattern getCountOfPagesPattern();

    protected void init(Document document) {
    }

    public int getCountOfPages(String html) {
        Pattern pattern = getCountOfPagesPattern();
        Matcher matcher = pattern.matcher(html);
        String info;
        if (matcher.find()) {
            info = matcher.group();
        } else {
            throw new RuntimeException("Не удалось определить количество страниц");
        }
        String[] split = info.split(" ");
        return Integer.parseInt(split[split.length - 1]);
    }
}
