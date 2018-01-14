package ru.ifmo.utils.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;
import ru.ifmo.entity.Topic;
import ru.ifmo.pools.MessagePool;
import ru.ifmo.utils.DialogueBuilder;
import ru.ifmo.utils.DialogueWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

public class DialogueWriterImpl implements DialogueWriter {

    private static final Logger LOG = Logger.getLogger(DialogueWriterImpl.class);
    private int totalCountFlushedDialogues = 0;
    private MessagePool messagePool = MessagePool.getInstance();
    private DialogueBuilder builder;

    @Inject
    public DialogueWriterImpl(DialogueBuilder builder) {
        this.builder = builder;
    }

    public void flushDialogues(String out, Topic topic) throws IOException {
        LOG.info("Total messages after " + topic + " analyzing is  " + messagePool.getPool().size());
        long startLoadingTime = System.currentTimeMillis();
        List<Message> leafMessages = messagePool.getLeafMessages();
        long endLoadingTime = System.currentTimeMillis();
        LOG.trace("Leaf message searching " + (endLoadingTime - startLoadingTime) + " ms");
        LOG.info("Total count of dialogues is " + leafMessages.size());

        startLoadingTime = System.currentTimeMillis();
        writeInFile(out, topic, leafMessages);
        endLoadingTime = System.currentTimeMillis();
        LOG.trace("Flushing took " + (endLoadingTime - startLoadingTime) + " ms");
    }

    protected void writeInFile(String out, Topic topic, List<Message> leafMessages) throws IOException {
        File file = new File(out);
        LOG.info("start writing in file " + out);
        if (!file.exists()) {
            boolean newFileCreated = file.createNewFile();
            LOG.info("New file Created" + newFileCreated);
        }
        int countFlushedDialogues = 0;
        for (Message leafMessage : leafMessages) {
            LOG.debug("Building dialogue with order number = " + leafMessage.getOrderNum());
            Dialogue dialogue = builder.build(leafMessage);
            if (dialogue.size() <= 3) {
                continue;
            }
            List<Dialogue> dialogues = dialogue.getSubDialogues(4);
            for (Dialogue currentDialogue: dialogues) {
                LOG.debug("Writing dialogue with order number = " + leafMessage.getOrderNum());
                int startOrderNum = MessagePool.getInstance().getFirstMessage(topic).getOrderNum();
                StringJoiner joiner = new StringJoiner("\n->");
                for (Message message : currentDialogue.getMessages()) {
                    if (message.getOrderNum() > startOrderNum) {
                        joiner.add(message.getOrderNum() + ")" + message.getText());
                    }
                }
                countFlushedDialogues++;
                totalCountFlushedDialogues++;
                Files.append(joiner.toString() + "\n\n", file, Charsets.UTF_8);
                messagePool.remove(dialogue);
            }

        }
        LOG.info("Writing in file " + out + " finished. Count of flushed dialogues " + countFlushedDialogues);
    }

    public int getTotalCountFlushedDialogues() {
        return totalCountFlushedDialogues;
    }
}
