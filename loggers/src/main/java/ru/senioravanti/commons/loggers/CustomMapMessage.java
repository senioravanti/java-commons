package ru.senioravanti.commons.loggers;

import org.apache.logging.log4j.message.MapMessage;

import java.util.HashMap;
import java.util.Map;

public class CustomMapMessage extends MapMessage<CustomMapMessage, Object> {
    private CustomMapMessage(Map<String, Object> map) {
        super(map);
    }

    public static CustomMapMessage of(
        String msg,
        Map<String, Object> params
    ) {
        return of(msg, params, null);
    }

    public static CustomMapMessage of(
        String msg,
        Throwable ex
    ) {
        return of(msg, null, ex);
    }

    public static CustomMapMessage of(
        String msg,
        Map<String, Object> params,
        Throwable ex
    ) {
        var map = new HashMap<String, Object>();
        map.put("msg", msg);
        if (params != null) {
            map.putAll(params);
        }
        if (ex != null) {
            map.put("ex", ExceptionView.of(ex));
        }
        return new CustomMapMessage(map);
    }

    public static CustomMapMessage of(Object obj) {
        var map = new HashMap<String, Object>();
        map.put("msg", obj);
        return new CustomMapMessage(map);
    }
}
