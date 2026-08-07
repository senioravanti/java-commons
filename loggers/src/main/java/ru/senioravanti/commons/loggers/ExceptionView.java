package ru.senioravanti.commons.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

record ExceptionView(
    String message,
    String type,
    List<StackFrame> stackFrames,
    ExceptionView cause,
    List<ExceptionView> suppressed
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

    private static List<StackFrame> extractStackFrames(LoggersConfig config, StackTraceElement[] stackTraceElements) {
        int externalFrameCount = 0;
        var stackFrames = new ArrayList<StackFrame>();
        for (var it : stackTraceElements) {
            var isInternal = it.getClassName().startsWith(config.basePackage());
            if (isInternal || externalFrameCount < config.maxExternalFrames()) {
                stackFrames.add(new StackFrame(it.getClassName(), it.getMethodName(), it.getLineNumber()));
            }
            if (!isInternal) {
                externalFrameCount++;
            }
        }
        return stackFrames;
    }

    private static ExceptionView of(Throwable ex, int depth) {
        var config = LoggersConfigHolder.getConfig();
        if (ex == null || depth > config.maxCauseDepth()) return null;
        var stackFrames = extractStackFrames(config, ex.getStackTrace());
        var suppressed = new ArrayList<ExceptionView>();
        for (var it : ex.getSuppressed()) {
            suppressed.add(ExceptionView.of(it, 0));
        }
        return new ExceptionView(ex.getMessage(), ex.getClass().getName(), stackFrames, ExceptionView.of(ex.getCause(), depth + 1), suppressed);
    }
}
