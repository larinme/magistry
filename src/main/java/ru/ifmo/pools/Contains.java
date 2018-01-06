package ru.ifmo.pools;

import ru.ifmo.entity.Message;

@FunctionalInterface
public interface Contains{
    boolean areEquals(Message first, Message second);
}
