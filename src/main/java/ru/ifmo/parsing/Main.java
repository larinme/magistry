package ru.ifmo.parsing;

import com.google.common.io.Resources;
import ru.ifmo.parsing.impl.KinopoiskForumParser;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Main {

    private static final String OUT;
    private static Properties properties = new Properties();

    static {
        URL out = Resources.getResource("out/scripts.txt");
        OUT = out.getPath();
        URL url = Resources.getResource("sources.properties");
        try {
            properties.load(url.openStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        String url = (String) properties.get("1");
        KinopoiskForumParser kinopoiskForum = new KinopoiskForumParser(OUT);
        kinopoiskForum.parse(url);

    }
}
