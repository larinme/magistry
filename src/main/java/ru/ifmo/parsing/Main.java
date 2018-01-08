package ru.ifmo.parsing;

import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ru.ifmo.parsing.impl.KinopoiskForumParser;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

public class Main {

    private static final String OUT;
    private static Properties properties = new Properties();
    private static final Logger log = Logger.getLogger(Main.class);

    static {
        String log4j = Resources.getResource("log4j.properties").getPath();
        PropertyConfigurator.configure(log4j);
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
        log.info("Start application");
        try {
            run();
        } catch (Exception e){
            log.error(e);
            throw e;
        }
        log.info("End application");
    }

    private static void run() throws IOException {
        Collection<Object> values = properties.values();
        for (Object value : values) {
            String url = (String) value;
            Parser parser = new KinopoiskForumParser(OUT);
            log.info("Start parsing " + url);
            parser.parse(url);
            log.info("Parsing successfully finished");
        }
    }
}
