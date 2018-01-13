package ru.ifmo.utils;

import ru.ifmo.entity.Dialogue;
import ru.ifmo.entity.Message;

public interface DialogueBuilder {

    Dialogue build(Message message);
}
