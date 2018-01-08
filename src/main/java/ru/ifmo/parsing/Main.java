package ru.ifmo.parsing;

import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ru.ifmo.parsing.impl.KinopoiskForumParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

public class Main {

    private static final String OUT = "dialogues/result.txt";
    private static final Logger log = Logger.getLogger(Main.class);
    private static Properties properties = new Properties();

    static {
        String log4j = Resources.getResource("log4j.properties").getPath();
        PropertyConfigurator.configure(log4j);
        URL url = Resources.getResource("sources.properties");
        try {
            properties.load(url.openStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        File file = new File(OUT);
        boolean isDeleted = file.delete();
        log.debug("Output directory was deleted " + isDeleted);
    }

    public static void main(String[] args) throws IOException {
        log.info("Start application");
        try {
            run();
        } catch (Exception e) {
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
