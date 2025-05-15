package com.freshworks.freddy.insights.helper;

import org.apache.commons.text.StringEscapeUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {
    public static String stackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stacktrace = sw.toString();
        return String.format("%s : %s : %s : %s :",
                ex.getClass(), ex.getMessage(), ex.getCause(), StringEscapeUtils.escapeJava(stacktrace))
                .replaceAll("\n", " >>> ");
    }
}
