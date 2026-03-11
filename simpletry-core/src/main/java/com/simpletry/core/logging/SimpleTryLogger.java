package com.simpletry.core.logging;

public final class SimpleTryLogger {

    private SimpleTryLogger() {
    }

    public static void log(String method,
                           String clazz,
                           Throwable e,
                           String tag) {

        System.err.println(
                "SIMPLETRY|" +
                        "method=" + method +
                        "|class=" + clazz +
                        "|exception=" + e.getClass().getSimpleName() +
                        "|message=" + e.getMessage() +
                        "|tag=" + tag
        );

        e.printStackTrace(System.err);
    }
}