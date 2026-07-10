package ru.senioravanti.commons.result;

@FunctionalInterface
public interface CheckedRunnable {
    void run() throws Exception;
}
