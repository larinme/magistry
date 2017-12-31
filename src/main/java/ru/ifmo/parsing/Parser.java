package ru.ifmo.parsing;

import java.io.IOException;

public interface Parser {

    void parse(String url) throws IOException;

    int getCountOfPages(String html);
}
