package ru.senioravanti.commons.loggers;

public final class LoggersConfigHolder {
    private static volatile LoggersConfig config = LoggersConfig.DEFAULT;

    private LoggersConfigHolder() {
    }

    public static LoggersConfig getConfig() {
        return config;
    }

    public static void setConfig(LoggersConfig config) {
        synchronized (LoggersConfig.class) {
            LoggersConfigHolder.config = config;
        }
    }
}
