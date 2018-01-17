package ru.ifmo.parsing;

import com.google.common.io.Resources;
import com.google.inject.Guice;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ru.ifmo.parsing.impl.KinopoiskForumParser;
import ru.ifmo.utils.Module;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

public class Main {

    private static final String BASE_DIR = System.getProperty("user.dir");
    private static final String OUT = BASE_DIR + "/dialogues/formatted/result.txt";
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
        File file = new File(BASE_DIR + OUT);
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
            File file = new File(OUT);
            boolean newFileCreated = file.createNewFile();
            log.debug("Output directory was deleted " + newFileCreated);

            Parser parser = Guice.createInjector(new Module()).getInstance(KinopoiskForumParser.class);
            log.info("Start parsing " + url);
            long startTime = System.currentTimeMillis();
            parser.parse(OUT, url);
            long endTime = System.currentTimeMillis();
            log.info("Parsing successfully finished. System spent " + (endTime - startTime) + " ms");
        }
    }
}
