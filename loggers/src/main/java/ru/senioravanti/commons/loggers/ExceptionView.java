package ru.senioravanti.commons.loggers;

import ru.senioravanti.commons.loggers.LoggersConfigHolder;

import java.util.ArrayList;
import java.util.List;

record ExceptionView(
    String message,
    String type,
    List<StackFrame> stackFrames,
    ExceptionView cause
) {
    record StackFrame(
        String className,
        String methodName,
        int lineNumber
    ) {
    }

    public static ExceptionView of(Throwable ex) {
        return of(ex, 0);
    }

    private static ExceptionView of(Throwable ex, int depth) {
        var config = LoggersConfigHolder.getConfig();
        if (ex == null || depth > config.maxCauseDepth()) {
            return null;
        }
        int externalFrameCount = 0;
        var stackFrames = new ArrayList<StackFrame>();
        for (var it : ex.getStackTrace()) {
            var isInternal = it.getClassName().startsWith(config.basePackage());
            if (isInternal || externalFrameCount < config.maxExternalFrames()) {
                stackFrames.add(new StackFrame(it.getClassName(), it.getMethodName(), it.getLineNumber()));
            }
            if (!isInternal) {
                externalFrameCount++;
            }
        }
        return new ExceptionView(ex.getMessage(), ex.getClass().getName(), stackFrames, ExceptionView.of(ex.getCause(), depth + 1));
    }
}
