package ru.senioravanti.commons.loggers;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;

/*
[25.07.2026, 12:01:56] INFO <main> ru.senioravanti.vkbot.App#start:71 {
  "msg": "staring app ..."
}
 */
@Plugin(
    name = "PrettyStructuredLoggingFormatter",
    category = Node.CATEGORY,
    elementType = Layout.ELEMENT_TYPE
)
public class PrettyStructuredLoggingFormatter extends AbstractStringLayout {
    private static final JsonMapper JSON = JsonMapper.builder()
        .defaultPrettyPrinter(new DefaultPrettyPrinter()
            .withSeparators(new Separators()
                .withObjectNameValueSpacing(Separators.Spacing.AFTER)))
        .build();

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private static final String RESET = "\u001B[0m";

    private enum AnsiColors {
        BLACK(30),
        RED(31),
        GREEN(32),
        YELLOW(33),
        BLUE(34),
        MAGENTA(35),
        CYAN(36),
        LIGHT_GRAY(37),
        DARK_GRAY(90),
        LIGHT_RED(91),
        LIGHT_GREEN(92),
        LIGHT_YELLOW(93),
        LIGHT_BLUE(94),
        LIGHT_MAGENTA(95),
        LIGHT_CYAN(96),
        WHITE(97);
        private final int code;

        AnsiColors(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static String colorize(int code, String s) {
        return "\u001B[" + code + "m" + s + RESET;
    }

    protected PrettyStructuredLoggingFormatter() {
        super(StandardCharsets.UTF_8);
    }

    @Override
    public String toSerializable(LogEvent evt) {
        var jsonEntry = new StringBuilder();
        // timestamp
        jsonEntry.append(colorize(AnsiColors.LIGHT_GRAY.getCode(), "[%s]".formatted(LocalDateTime.ofInstant(Instant.ofEpochMilli(evt.getTimeMillis()), ZoneId.systemDefault()).format(TIMESTAMP_FORMATTER))));
        jsonEntry.append(" ");
        // level
        var level = evt.getLevel().name();
        var levelColor = switch (level) {
            case "DEBUG" -> AnsiColors.LIGHT_BLUE;
            case "INFO" -> AnsiColors.LIGHT_MAGENTA;
            case "WARN" -> AnsiColors.LIGHT_YELLOW;
            case "ERROR" -> AnsiColors.LIGHT_RED;
            default -> AnsiColors.LIGHT_CYAN;
        };
        jsonEntry.append(colorize(levelColor.getCode(), level));
        jsonEntry.append(" ");
        // context
        var src = evt.getSource();
        jsonEntry.append(colorize(AnsiColors.GREEN.getCode(), "<%s> %s#%s:%d".formatted(evt.getThreadName(), src.getClassName(), src.getMethodName(), src.getLineNumber())));
        jsonEntry.append(" ");
        // structured message
        var msg = evt.getMessage();
        var structuredMessage = new LinkedHashMap<String, Object>();
        if (msg instanceof CustomMapMessage customMapMessage) {
            structuredMessage.putAll(customMapMessage.toMap());
        } else {
            structuredMessage.put("msg", msg.getFormattedMessage());
        }
        var mdc = evt.getContextData().toMap();
        if (!mdc.isEmpty()) structuredMessage.put("mdc", mdc);
        jsonEntry.append(JSON
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(structuredMessage));
        jsonEntry.append("\n");
        return jsonEntry.toString();
    }

    @PluginFactory
    public static PrettyStructuredLoggingFormatter createLayout() {
        return new PrettyStructuredLoggingFormatter();
    }
}
