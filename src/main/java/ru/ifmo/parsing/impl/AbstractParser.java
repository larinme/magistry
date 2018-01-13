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
import ru.ifmo.entity.utils.ComparableDocument;
import ru.ifmo.parsing.Parser;
import ru.ifmo.pools.*;
import ru.ifmo.utils.DialogueBuilder;
import ru.ifmo.utils.DocumentDownloadingThread;
import ru.ifmo.utils.impl.DialogueBuilderImpl;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Logger LOG = Logger.getLogger(AbstractParser.class);
    private static final int COUNT_OF_THREADS = 4;
    private int totalCountFlushedDialogues = 0;

    private final DialogueBuilder dialogueBuilder;


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

    public AbstractParser(String out, DialogueBuilder dialogueBuilder) {
        this.out = out;
        this.dialogueBuilder = dialogueBuilder;
    }

    public AbstractParser(String out) {
        this.out = out;
        this.dialogueBuilder = DialogueBuilderImpl.getInstance();
    }

    protected void init(Document document) {
        String url = document.baseUri();
        Source source = sourcePool.putIfNotExists(getSourceName(), url);
        String title = document.select(getTitleQuery()).text();
        topic = topicPool.putIfNotExists(source, url, getThema(), title);
    }

    private SortedSet<ComparableDocument> getDocuments(String url, int pageCount) {
        SortedSet<ComparableDocument> documents = new TreeSet<>();
        Collection<DocumentDownloadingThread> activeThreads = new ArrayList<>();
        List<DocumentDownloadingThread> threads = new ArrayList<>();
        for (int i = 0; i < COUNT_OF_THREADS; i++) {
            int range = (pageCount / COUNT_OF_THREADS) + 1;
            int startPage = (range * i) + 1;
            int endPage = Math.min(pageCount, (range * (i + 1)));
            threads.add(new DocumentDownloadingThread(url + "&" + getPageNumberParameter(), startPage, endPage));
        }

        startDownloadThreads(activeThreads, threads);
        waitPageDownloadFinish(activeThreads, threads);

        for (DocumentDownloadingThread thread : threads) {
            SortedSet<ComparableDocument> threadDocuments = thread.getDocuments();
            documents.addAll(threadDocuments);
        }
        return documents;
    }

    private void waitPageDownloadFinish(Collection<DocumentDownloadingThread> activeThreads, List<DocumentDownloadingThread> threads) {
        while (!activeThreads.isEmpty()) {
            for (DocumentDownloadingThread thread : threads) {
                if (thread.getState().equals(Thread.State.RUNNABLE)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    activeThreads.remove(thread);
                }
            }
        }
    }

    private void startDownloadThreads(Collection<DocumentDownloadingThread> activeThreads, List<DocumentDownloadingThread> threads) {
        for (DocumentDownloadingThread thread : threads) {
            activeThreads.add(thread);
            thread.start();
        }
    }

    public void parse(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        init(document);

        int countOfPages = getCountOfPages(document.html());
        LOG.info("Downloading pages...");
        long startLoadingTime = System.currentTimeMillis();
        SortedSet<ComparableDocument> documents = getDocuments(url, countOfPages);
        long endLoadingTime = System.currentTimeMillis();
        LOG.info("Downloading pages finished. System spent " + (endLoadingTime - startLoadingTime) + " ms");

        for (ComparableDocument comparableDocument : documents) {
            document = comparableDocument.getDocument();
            int currentPage = comparableDocument.getOrderNumber();
            parsePostsOnPage(getPosts(document), currentPage);
            LOG.info("The page " + currentPage + " has been parsed");

            if (currentPage % 10 == 0) {
                removeSingleMessages(25 * (currentPage - 3));
            }
            if ((currentPage - 5) % 50 == 0) {
                flushDialogues();
            }
        }

        flushDialogues();
        LOG.info("Total count of flushed dialogues: " + totalCountFlushedDialogues);
    }

    protected void parsePostsOnPage(Elements posts, int currentPage) {
        for (int currentMessage = 1; currentMessage <= posts.size(); currentMessage++) {
            Element post = posts.get(currentMessage - 1);
            try {
                long startTime = System.currentTimeMillis();
                parseMessage(post, currentPage, currentMessage);
                long endTime = System.currentTimeMillis();
                LOG.trace("Message was parsed for " + (endTime - startTime));
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
        LOG.debug("Post date " + dateString);
        Element text = post.getElementById(getPostQuery() + messageId);
        String textMessage = text.text();
        Date date;
        try {
            date = getDateFormat().parse(dateString);
        } catch (ParseException e) {
            date = new Date();
        }
        int orderNum = 25 * (pageNumber - 1) + currentPost;
        LOG.debug("Order number " + orderNum);
        String pattern = "<div style=\"margin:20px; margin-top:5px; \">.+?";
        String html = text.html();
        String[] split = html.split(pattern);
        for (String part : split) {
            if (part.equals("")) {
                continue;
            }
            Message message = messagePool.put(topic, author, textMessage, orderNum, date);
            split(part, message);
            message.setText(message.getText().replaceFirst("^\\s*", ""));
        }
    }


    protected void split(String html, Message message) {
        Map<TokenType, List<Token>> tokens = new HashMap<>();
        String text = cleanMessage(html);
        long startTime = System.currentTimeMillis();
        text = substituteTokens(message, tokens, text);
        long endTime = System.currentTimeMillis();
        LOG.trace("Token substitution took " + (endTime - startTime));

        LOG.debug("Tokens: " + text);
        startTime = System.currentTimeMillis();
        performTokenAnalyzing(message, tokens, text);
        endTime = System.currentTimeMillis();
        LOG.trace("Token substitution took " + (endTime - startTime));
        LOG.debug("Token parsing ended");

    }

    protected void performTokenAnalyzing(Message message, Map<TokenType, List<Token>> tokens, String text) {
        String[] split = text.split(" ");
        StringBuilder builder = new StringBuilder();
        int orderNum = 1;
        LOG.debug("Token parsing started");

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
                    LOG.debug("Text token " + textToken);
                }
                builder = new StringBuilder();
                TokenType type = TokenType.PRESENTERS.get(element.charAt(1));
                LOG.debug("Token type " + type);
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
                    LOG.debug("Reference " + reference);
                }
                LOG.debug("Token " + token);
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
            LOG.debug("token type statistic: type " + tokenType + ", elements " + list);
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
        LOG.debug("Author name " + authorName);
        AuthorPool authorPool = AuthorPool.getInstance();
        return authorPool.putIfNotExists(authorName, "");
    }

    private void removeSingleMessages(int range) {
        long startLoadingTime = System.currentTimeMillis();
        int poolVolume = messagePool.clear(range);
        long endLoadingTime = System.currentTimeMillis();
        LOG.trace("Single messages removing took " + (endLoadingTime - startLoadingTime) + " ms");
        LOG.info("Pool size = " + poolVolume);

    }

    private void flushDialogues() throws IOException {
        LOG.info("Total messages after " + topic + " analyzing is  " + messagePool.getPool().size());
        long startLoadingTime = System.currentTimeMillis();
        List<Message> leafMessages = messagePool.getLeafMessages();
        long endLoadingTime = System.currentTimeMillis();
        LOG.trace("Leaf message searching " + (endLoadingTime - startLoadingTime) + " ms");
        LOG.info("Total count of dialogues is " + leafMessages.size());

        startLoadingTime = System.currentTimeMillis();
        writeInFile(leafMessages);
        endLoadingTime = System.currentTimeMillis();
        LOG.trace("Flushing took " + (endLoadingTime - startLoadingTime) + " ms");

        startLoadingTime = System.currentTimeMillis();
        messagePool.remove(leafMessages, topic);
        endLoadingTime = System.currentTimeMillis();
        LOG.trace("Removing flushed messages " + (endLoadingTime - startLoadingTime) + " ms");
    }

    protected void writeInFile(List<Message> leafMessages) throws IOException {
        File file = new File(out);
        LOG.info("start writing in file " + out);
        if (!file.exists()) {
            boolean newFileCreated = file.createNewFile();
            LOG.info("New file Created" + newFileCreated);
        }
        int countFlushedDialogues = 0;
        for (Message leafMessage : leafMessages) {
            LOG.debug("Building dialogue with order number = " + leafMessage.getOrderNum());
            Dialogue dialogue = dialogueBuilder.build(leafMessage);
            if (dialogue.size() <= 3) {
                continue;
            }
            LOG.debug("Writing dialogue with order number = " + leafMessage.getOrderNum());
            int startOrderNum = messagePool.getFirstMessage(topic).getOrderNum();
            StringJoiner joiner = new StringJoiner("\n->");
            for (Message message : dialogue.getMessages()) {
                if (message.getOrderNum() > startOrderNum) {
                    joiner.add(message.getOrderNum() + ")" + message.getText());
                }
            }
            countFlushedDialogues++;
            totalCountFlushedDialogues++;
            Files.append(joiner.toString() + "\n\n", file, Charsets.UTF_8);
        }
        LOG.info("Writing in file " + out + " finished. Count of flushed dialogues " + countFlushedDialogues);
    }

}
