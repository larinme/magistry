package ru.ifmo.parsing.impl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ifmo.entity.Author;
import ru.ifmo.entity.Message;
import ru.ifmo.entity.Source;
import ru.ifmo.entity.Topic;
import ru.ifmo.pools.AuthorPool;
import ru.ifmo.pools.MessagePool;
import ru.ifmo.pools.SourcePool;
import ru.ifmo.pools.TopicPool;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class KinopoiskForumParser extends AbstractParser {

    private static final String TITLE_QUERY = ".navbar > strong";
    private static final String POSTS_QUERY = "div.posts > #table";
    private static final String DATE_QUERY = "td.thead";
    private static final String USERNAME_QUERY = "a.bigusername";
    private static final String POST_QUERY = "post_message_";
    private static final Pattern COUNT_OF_PAGES_PATTERN = Pattern.compile("Страница \\d* из \\d*");
    private static final DateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.ENGLISH);
    private final String out;
    private Source source;
    private Topic topic;

    public KinopoiskForumParser(String out) {
        this.out = out;
    }

    @Override
    protected void init(Document document) {
        String url = document.baseUri();

        SourcePool sourcePool = SourcePool.getInstance();
        source = sourcePool.putIfNotExists("Kinopoisk", url);

        TopicPool topicTopicPool = TopicPool.getInstance();
        String title = document.select(TITLE_QUERY).text();
        topic = topicTopicPool.putIfNotExists(source, url, "Кино", title);
    }

    public void parse(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        init(document);
        int countOfPages = getCountOfPages(document.html());
        for (int currentPage = 1; currentPage <= countOfPages; currentPage++) {
            System.out.println("Страница  " + currentPage);
            document = Jsoup.connect(url + "&page=" + currentPage).get();
            Elements posts = document.getElementById("posts").getElementsByClass("tborder");
            for (int currentMessage = 1; currentMessage < posts.size(); currentMessage++) {
                Element post = posts.get(currentMessage - 1);
                Author author = parseAuthor(post);
                Message message = parseMessage(post, author, currentPage, currentMessage);
            }

        }
    }

    private Author parseAuthor(Element post) {
        String authorName = post.select(USERNAME_QUERY).text();
        AuthorPool authorPool = AuthorPool.getInstance();
        return authorPool.putIfNotExists(authorName, "");
    }

    private Message parseMessage(Element post, Author author, int pageNumber, int currentPost) {
        long messageId = Long.parseLong(post.id().replace("post", ""));
        String dateString = post.select(DATE_QUERY).get(0).text();
        Element text = post.getElementById(POST_QUERY + messageId);
        String textMessage = text.text();
        Date date;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            date = new Date();
        }
        int orderNum = 25 * (pageNumber - 1) + currentPost;
        return MessagePool.getInstance().put(topic, author, null, textMessage, orderNum, date);
    }

    Pattern getCountOfPagesPattern() {
        return COUNT_OF_PAGES_PATTERN;
    }
}
