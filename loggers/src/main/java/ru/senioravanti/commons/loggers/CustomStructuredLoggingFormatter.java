package ru.senioravanti.commons.loggers;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Plugin(
    name = "CustomStructuredLoggingFormatter",
    category = Node.CATEGORY,
    elementType = Layout.ELEMENT_TYPE,
    printObject = true
)
public class CustomStructuredLoggingFormatter extends AbstractStringLayout {
    private final JsonMapper objectMapper = JsonMapper.builder().build();

    protected CustomStructuredLoggingFormatter() {
        super(StandardCharsets.UTF_8);
    }

    @PluginFactory
    public static CustomStructuredLoggingFormatter createLayout() {
        return new CustomStructuredLoggingFormatter();
    }

    @Override
    public String toSerializable(LogEvent evt) {
        var logEntry = new LinkedHashMap<String, Object>(evt.getContextData().toMap());

        logEntry.put("time", LocalDateTime.ofInstant(Instant.ofEpochMilli(evt.getInstant().getEpochMillisecond()), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logEntry.put("level", evt.getLevel().name());
        logEntry.put("thread", evt.getThreadName());

        var source = evt.getSource();
        Map<String, Object> sourceMap;
        if (source != null) {
            sourceMap = new LinkedHashMap<>();
            sourceMap.put("class", source.getClassName());
            sourceMap.put("method", source.getMethodName());
            sourceMap.put("line", source.getLineNumber());
        } else {
            sourceMap = Map.of();
        }
        logEntry.put("source", sourceMap);

        var msg = evt.getMessage();
        if (msg instanceof CustomMapMessage customMapMessage) {
            logEntry.putAll(customMapMessage.toMap());
        } else {
            logEntry.put("message", evt.getMessage() != null ? evt.getMessage().getFormattedMessage() : "");
        }

        var mdc = evt.getContextData().toMap();
        if (!mdc.isEmpty()) logEntry.put("mdc", mdc);

        return objectMapper.writeValueAsString(logEntry) + "\n";
    }
}
