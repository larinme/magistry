package ru.ifmo.parsing.impl;

import org.jsoup.nodes.Document;
import ru.ifmo.parsing.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractParser implements Parser {

    abstract Pattern getCountOfPagesPattern();

    protected void init(Document document){}

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
