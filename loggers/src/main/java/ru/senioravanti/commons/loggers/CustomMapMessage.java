package ru.senioravanti.commons.loggers;

import org.apache.logging.log4j.message.Message;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record CustomMapMessage(
    String message,
    Throwable ex,
    Map<String, Object> params
) implements Message {
    public CustomMapMessage(
        String msg,
        Map<String, Object> params
    ) {
        this(msg, null, params);
    }

    public CustomMapMessage(
        String msg,
        Throwable ex
    ) {
        this(msg, ex, null);
    }

    public CustomMapMessage(Object obj) {
        this(null, null, Map.of("obj", obj));
    }

    @SafeVarargs
    public static CustomMapMessage of(
        String msg,
        Throwable ex,
        Map.Entry<String, ?>... entries
    ) {
        Map<String, Object> params = entries.length > 0
            ? Arrays.stream(entries).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, LinkedHashMap::new))
            : null;
        return new CustomMapMessage(msg, ex, params);
    }

    @SafeVarargs
    public static CustomMapMessage of(
        String msg,
        Map.Entry<String, ?>... entries
    ) {
        return of(msg, null, entries);
    }

    public Map<String, Object> toMap() {
        var map = new LinkedHashMap<String, Object>();
        if (message != null) map.put("msg", message);
        if (params != null) map.putAll(params);
        if (ex != null) map.put("ex", ExceptionView.of(ex));
        return map;
    }

    @Override
    public String getFormattedMessage() {
        return toMap().toString();
    }

    @Override
    public Object[] getParameters() {
        if (params == null) return null;
        return params.values().toArray();
    }

    @Override
    public Throwable getThrowable() {
        return ex;
    }
}
