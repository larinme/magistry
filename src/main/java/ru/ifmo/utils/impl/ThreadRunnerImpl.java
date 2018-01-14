package ru.ifmo.utils.impl;

import ru.ifmo.utils.ThreadRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThreadRunnerImpl<T extends Thread> implements ThreadRunner<T> {

    private Collection<T> activeThreads = new ArrayList<>();

    @Override
    public void start(List<T> threads) {
        run(threads);
        waitFinishing(threads);
    }

    private void waitFinishing(List<T> threads) {
        while (!activeThreads.isEmpty()) {
            for (T thread : threads) {
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

    private void run(List<T> threads) {
        for (T thread : threads) {
            activeThreads.add(thread);
            thread.start();
        }
    }
}
