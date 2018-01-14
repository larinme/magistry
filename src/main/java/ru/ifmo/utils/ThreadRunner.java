package ru.ifmo.utils;

import java.util.List;

public interface ThreadRunner<T extends Thread> {

    void start(List<T> threads);
}
