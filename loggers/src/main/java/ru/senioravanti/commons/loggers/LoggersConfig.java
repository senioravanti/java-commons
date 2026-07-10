package ru.senioravanti.commons.loggers;

public record LoggersConfig(
    String basePackage,
    int maxExternalFrames,
    int maxCauseDepth
) {
    public static final LoggersConfig DEFAULT = new LoggersConfig("ru.lip", 3, 3);
}
