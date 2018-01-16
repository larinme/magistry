package ru.ifmo.utils.impl;

import org.apache.log4j.Logger;
import ru.ifmo.parsing.Main;
import ru.ifmo.utils.ThreadRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThreadRunnerImpl<T extends Thread> implements ThreadRunner<T> {

    private Collection<T> activeThreads = new ArrayList<>();
    private static final Logger log = Logger.getLogger(ThreadRunnerImpl.class);


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
                    log.info(thread.getName() + " finished ");
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
