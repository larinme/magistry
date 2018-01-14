package ru.ifmo.parsing;

import java.io.IOException;

public interface Parser {

    void parse(String out, String url) throws IOException;

}
