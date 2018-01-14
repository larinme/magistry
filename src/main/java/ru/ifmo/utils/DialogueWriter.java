package ru.ifmo.utils;

import ru.ifmo.entity.Topic;

import java.io.IOException;

public interface DialogueWriter {

    void flushDialogues(String out, Topic topic) throws IOException;

    int getTotalCountFlushedDialogues();
}
