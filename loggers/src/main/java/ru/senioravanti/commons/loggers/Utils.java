package ru.senioravanti.commons.loggers;

import org.apache.logging.log4j.core.LogEvent;

class Utils {
    static String getThreadName(LogEvent evt) {
        var threadName = evt.getThreadName();
        if (threadName != null && !threadName.isBlank()) return threadName;
        var currentThread = Thread.currentThread();
        var threadId = evt.getThreadId();
        if (threadId != currentThread.threadId()) return String.valueOf(threadId);
        return "%s-thread-%d".formatted(currentThread.isVirtual() ? "virtual" : "platform", threadId);
    }
}
