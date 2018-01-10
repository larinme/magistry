package ru.ifmo.parsing.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ifmo.entity.*;
import ru.ifmo.parsing.Parser;
import ru.ifmo.pools.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractParser implements Parser {

    protected static final Function<String, String> DEFAULT_TOKEN_TYPE_PROCESSOR = value -> value;
    protected static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(\\B(#[a-zA-Z]+\\b)(?!;))");
    private static final Pattern LINK_PATTERN = Pattern.compile("((?i)<a([^>]+)>(.+?)</a>)");
    private static final Pattern QUOTE_PATTERN = Pattern.compile("<div.*Цитата:.*Сообщение\\s*от.*</div>");
    private static final Pattern EMOTICON_PATTERN = Pattern.compile("<img\\s*src=\"[\\w|/]*.gif\"[\\s*|(\\w*=\"\\w*\")]*/>");
    protected final SourcePool sourcePool = SourcePool.getInstance();
    protected final MessagePool messagePool = MessagePool.getInstance();
    protected final TopicPool topicPool = TopicPool.getInstance();
    protected final TokenPool tokenPool = TokenPool.getInstance();
    protected final String out;
    protected Topic topic;
    protected static final Map<TokenType, Pattern> PATTERNS = ImmutableMap.<TokenType, Pattern>builder()
            .put(TokenType.HASH_TAG, HASHTAG_PATTERN)
            .put(TokenType.EMOTICON, EMOTICON_PATTERN)
            .put(TokenType.LINK, LINK_PATTERN)
            .put(TokenType.QUOTE, QUOTE_PATTERN)
            .build();
    protected static final Map<String, String> CLEAN_TEXT_MAP = ImmutableMap.<String, String>builder()
            .put("<br\\s/>", "")
            .put("&quot;", "\"")
            .put("\n", " ")
            .build();
    private static final Logger log = Logger.getLogger(KinopoiskForumParser.class);


    abstract Pattern getCountOfPagesPattern();

    abstract String getSourceName();

    abstract String getThema();

    abstract String getTitleQuery();

    abstract String getAuthorQuery();

    abstract String getDateQuery();

    abstract String getPostQuery();

    abstract String getPageNumberParameter();

    abstract DateFormat getDateFormat();

    abstract Map<TokenType, Function<String, String>> getTokenTypeProcessor();

    abstract Elements getPosts(Document document);

    public AbstractParser(String out) {
        this.out = out;
    }

    protected void init(Document document) {
        String url = document.baseUri();
        Source source = sourcePool.putIfNotExists(getSourceName(), url);
        String title = document.select(getTitleQuery()).text();
        topic = topicPool.putIfNotExists(source, url, getThema(), title);
    }

    public void parse(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        init(document);
        int countOfPages = getCountOfPages(document.html());
        for (int currentPage = 1; currentPage <= countOfPages; currentPage++) {
            log.info("Page analyzing started #" + currentPage);
            long startLoadingTime = System.currentTimeMillis();
            document = Jsoup.connect(url + "&" + getPageNumberParameter() + "=" + currentPage).get();
            long endLoadingTime = System.currentTimeMillis();
            log.info("Page has loaded. System spent " + (endLoadingTime - startLoadingTime) + " ms");
            parsePostsOnPage(getPosts(document), currentPage);
            log.info("The page " + currentPage + " has been parsed");

            removeSingleMessages(currentPage);
            removeDialogues(currentPage);
        }
    }

    protected void parsePostsOnPage(Elements posts, int currentPage) {
        for (int currentMessage = 1; currentMessage <= posts.size(); currentMessage++) {
            Element post = posts.get(currentMessage - 1);
            try {
                parseMessage(post, currentPage, currentMessage);
            } catch (Exception e) {
                throw new RuntimeException("Error occurred while analyzing message #"
                        + currentMessage + " on page #" + currentPage, e);
            }
        }
    }

    protected void parseMessage(Element post, int pageNumber, int currentPost) {
        Author author = parseAuthor(post);
        long messageId = Long.parseLong(post.id().replace("post", ""));
        String dateString = post.select(getDateQuery()).get(0).text();
        log.debug("Post date " + dateString);
        Element text = post.getElementById(getPostQuery() + messageId);
        String textMessage = text.text();
        Date date;
        try {
            date = getDateFormat().parse(dateString);
        } catch (ParseException e) {
            date = new Date();
        }
        int orderNum = 25 * (pageNumber - 1) + currentPost;
        log.debug("Order number " + orderNum);
        Message message = messagePool.put(topic, author, textMessage, orderNum, date);
        split(text.html(), message);

    }

    protected void writeInFile(List<Message> leafMessages) throws IOException {
        File file = new File(out);
        log.info("start writing in file " + out);
        if (!file.exists()) {
            boolean newFileCreated = file.createNewFile();
            log.info("New file Created" + newFileCreated);
        }
        for (Message leafMessage : leafMessages) {
            List<String> dialogue = buildDialogue(leafMessage);
            StringJoiner joiner = new StringJoiner("\n->");
            for (String dialogueText : dialogue) {
                joiner.add(dialogueText);
            }
            Files.append(joiner.toString() + "\n\n", file, Charsets.UTF_8);
        }
        log.info("Writing in file " + out + " finished");
    }

    protected void split(String html, Message message) {
        Map<TokenType, List<Token>> tokens = new HashMap<>();

        String text = cleanMessage(html);
        text = substituteTokens(message, tokens, text);

        log.debug("Tokens: " + text);
        performTokenAnalyzing(message, tokens, text);
        log.debug("Token parsing ended");

    }

    protected void performTokenAnalyzing(Message message, Map<TokenType, List<Token>> tokens, String text) {
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
                log.debug("Token type " + type);
                int index = Integer.parseInt(String.valueOf(element.charAt(2)));
                Token token = tokens.get(type).get(index);
                token.setOrderNumber(orderNum);
                Map<TokenType, Function<String, String>> tokenTypeProcessor = getTokenTypeProcessor();
                String newValue = tokenTypeProcessor.get(type).apply(token.getValue());
                token.setValue(newValue);
                if (type.equals(TokenType.QUOTE)) {
                    message.setText(text.replaceAll("\\$\\w\\d\\$", ""));
                    Message reference = messagePool.getMessageByText(token.getValue(), topic, messagePool.getFirstMessage(topic));
                    message.setReference(reference);
                    log.debug("Reference " + reference);
                }
                log.debug("Token " + token);
            }
        }
    }

    private String substituteTokens(Message message, Map<TokenType, List<Token>> tokens, String text) {
        for (Map.Entry<TokenType, Pattern> entry : PATTERNS.entrySet()) {
            Pattern pattern = entry.getValue();
            TokenType tokenType = entry.getKey();
            List<Token> list = new ArrayList<>();
            Matcher matcher = pattern.matcher(text);
            int currentTypeIndex = 0;
            while (matcher.find()) {
                Token token = tokenPool.putIfNotExists(tokenType, matcher.group(), message, 1);
                list.add(token);
                text = text.replaceFirst(pattern.pattern(), " \\$" + tokenType.code() + currentTypeIndex++ + "\\$ ");
                tokens.put(tokenType, list);
            }
            log.debug("token type statistic: type " + tokenType + ", elements " + list);
        }
        return text;
    }

    private String cleanMessage(String text) {
        String cleanText = text;
        for (Map.Entry<String, String> entry : CLEAN_TEXT_MAP.entrySet()) {
            cleanText = cleanText.replaceAll(entry.getKey(), entry.getValue());
        }
        return cleanText;
    }

    private List<String> buildDialogue(Message leafMessage) {
        Set<Message> dialogues = new TreeSet<>(Comparator.comparingInt(Message::getOrderNum));
        Message reference = leafMessage;
        while (reference != null) {
            dialogues.add(reference);
            reference = reference.getReference();
        }
        int startOrderNum = messagePool.getFirstMessage(topic).getOrderNum();
        return dialogues.stream()
                .filter(
                        msg -> msg.getOrderNum() > startOrderNum)
                .map(Message::getText)
                .collect(Collectors.toList());
    }

    private int getCountOfPages(String html) {
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

    private Author parseAuthor(Element post) {
        String authorName = post.select(getAuthorQuery()).text();
        log.debug("Author name " + authorName);
        AuthorPool authorPool = AuthorPool.getInstance();
        return authorPool.putIfNotExists(authorName, "");
    }

    private void removeDialogues(int currentPage) throws IOException {
        if ((currentPage - 5) % 50 == 0) {
            log.info("Total messages after " + topic + " analyzing is  " + messagePool.getPool().size());
            List<Message> leafMessages = messagePool.getLeafMessages();
            log.info("Total count of dialogues is " + leafMessages.size());

            writeInFile(leafMessages);
            messagePool.remove(leafMessages, topic);
        }
    }

    private void removeSingleMessages(int currentPage) {
        if (currentPage % 10 == 0) {
            int poolVolume = messagePool.clear(25 * (currentPage - 3));
            log.info("Pool size = " + poolVolume);
        }
    }
}
