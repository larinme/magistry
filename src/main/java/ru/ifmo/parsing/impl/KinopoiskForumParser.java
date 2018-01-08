package ru.ifmo.parsing.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ifmo.entity.*;
import ru.ifmo.pools.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KinopoiskForumParser extends AbstractParser {

    private static final String TITLE_QUERY = ".navbar > strong";
    private static final String DATE_QUERY = "td.thead";
    private static final String USERNAME_QUERY = "a.bigusername";
    private static final String POST_QUERY = "post_message_";
    private static final Pattern COUNT_OF_PAGES_PATTERN = Pattern.compile("Страница \\d* из \\d*");
    private static final DateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.ENGLISH);
    private static final Logger log = Logger.getLogger(KinopoiskForumParser.class);
    private final String out;
    private final SourcePool sourcePool = SourcePool.getInstance();
    private final MessagePool messagePool = MessagePool.getInstance();
    private final TopicPool topicPool = TopicPool.getInstance();
    private Topic topic;
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
                        if (!s.equals(" ") && !s.equals("")) {
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

    public KinopoiskForumParser(String out) {
        this.out = out;
    }

    @Override
    protected void init(Document document) {
        String url = document.baseUri();
        Source source = sourcePool.putIfNotExists("Kinopoisk", url);
        String title = document.select(TITLE_QUERY).text();
        topic = topicPool.putIfNotExists(source, url, "Кино", title);
    }

    public void parse(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        init(document);
        int countOfPages = getCountOfPages(document.html());
        for (int currentPage = 1; currentPage <= countOfPages; currentPage++) {
            log.info("Page analyzing started #" + currentPage);
            document = Jsoup.connect(url + "&page=" + currentPage).get();
            Elements posts = document.getElementById("posts").getElementsByClass("tborder");
            for (int currentMessage = 1; currentMessage <= posts.size(); currentMessage++) {
                Element post = posts.get(currentMessage - 1);
                try {
                    parseMessage(post, currentPage, currentMessage);

                } catch (Exception e) {
                    throw new RuntimeException("Error occurred while analyzing message #"
                            + currentMessage + " on page #" + currentPage, e);
                }
            }

            log.info("The page " + currentPage + " has been parsed");
        }
        writeInFile();

    }

    private void writeInFile() throws IOException {
        log.info("Total messages after " + topic + " analyzing is " + messagePool.getPool());
        List<Message> leafMessages = messagePool.getLeafMessages();
        log.info("Total count of dialogues is " + leafMessages.size());

        File file = new File(out);
        for (Message leafMessage : leafMessages) {
            List<String> dialogue = buildDialogue(leafMessage);
            StringBuilder builder = new StringBuilder();
            for (String dialogueText : dialogue) {
                builder.append(dialogueText).append("\n ->");
            }
            String formattedDialogue = builder.toString();
            String finalStringDialogue = formattedDialogue.substring(0, formattedDialogue.length() - 2);
            Files.append(finalStringDialogue + "\n\n",
                    file,
                    Charsets.UTF_8);
        }
    }

    private List<String> buildDialogue(Message leafMessage) {
        Set<Message> dialogues = new TreeSet<>(Comparator.comparingInt(Message::getOrderNum));
        Message reference = leafMessage;
        while (reference != null) {
            dialogues.add(reference);
            reference = reference.getReference();
        }
        return dialogues.stream().map(Message::getText).collect(Collectors.toList());
    }

    private Author parseAuthor(Element post) {
        String authorName = post.select(USERNAME_QUERY).text();
        log.debug("Author name " + authorName);
        AuthorPool authorPool = AuthorPool.getInstance();
        return authorPool.putIfNotExists(authorName, "");
    }

    private void parseMessage(Element post, int pageNumber, int currentPost) {
        Author author = parseAuthor(post);
        long messageId = Long.parseLong(post.id().replace("post", ""));
        String dateString = post.select(DATE_QUERY).get(0).text();
        log.debug("Post date " + dateString);
        Element text = post.getElementById(POST_QUERY + messageId);
        String textMessage = text.text();
        Date date;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            date = new Date();
        }
        int orderNum = 25 * (pageNumber - 1) + currentPost;
        log.debug("Order number " + orderNum);
        Message message = messagePool.put(topic, author, textMessage, orderNum, date);
        split(text.html(), message);

    }

    private void split(String html, Message message) {
        TokenPool tokenPool = TokenPool.getInstance();
        Map<TokenType, List<Token>> tokens = new HashMap<>();
        String text = html
                .replaceAll("<br\\s/>", "")
                .replaceAll("&quot;", "\"")
                .replaceAll("\n", " ");
        for (Map.Entry<TokenType, Pattern> entry : PATTERNS.entrySet()) {
            Pattern pattern = entry.getValue();
            TokenType tokenType = entry.getKey();
            List<Token> list = new ArrayList<>();
            Matcher matcher = pattern.matcher(text);
            int currentTypeIndex = 0;
            while (matcher.find()) {
                Token token = tokenPool.putIfNotExists(tokenType, matcher.group(), message, 1);
                list.add(token);
                text = text.replaceFirst(pattern.pattern(), "\\$" + tokenType.code() + currentTypeIndex++ + "\\$ ");
                tokens.put(tokenType, list);
            }
            log.debug("token type statistic: type " + tokenType + ", elements " + list);
        }

        log.debug("Tokens: " + text);
        String[] split = text.split(" ");
        StringBuilder builder = new StringBuilder();
        int orderNum = 1;
        log.debug("Token parsing started");
        for (int i = 0; i < split.length; i++) {
            String element = split[i];
            if (!Pattern.compile("\\$\\w\\d\\$").matcher(element).find()) {
                builder.append(element).append(" ");
                if (i + 1 == split.length) {
                    tokenPool.putIfNotExists(TokenType.PLAINT_TEXT, builder.toString(), message, orderNum);
                }
            } else {
                if (builder.length() > 0) {
                    Token textToken = tokenPool.putIfNotExists(TokenType.PLAINT_TEXT, builder.toString(), message, orderNum);
                    log.debug("Text token " + textToken);
                }
                builder = new StringBuilder();
                TokenType type = TokenType.PRESENTERS.get(element.charAt(1));
                int index = Integer.parseInt(String.valueOf(element.charAt(2)));
                Token token = tokens.get(type).get(index);
                token.setOrderNumber(orderNum);
                String newValue = TOKEN_TYPE_PROCESSORS.get(type).apply(token.getValue());
                token.setValue(newValue);
                log.debug("Token type " + type + "token " + token);
                if (type.equals(TokenType.QUOTE)) {
                    message.setText(text.replaceAll("\\$\\w\\d\\$", ""));
                    Message reference = messagePool.getMessageByText(token.getValue(), topic, messagePool.getFirstMessage(topic));
                    message.setReference(reference);
                    log.debug("Reference " + reference);
                }
            }
        }
        log.debug("Token parsing ended");

    }

    Pattern getCountOfPagesPattern() {
        return COUNT_OF_PAGES_PATTERN;
    }
}
