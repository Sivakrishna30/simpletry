package com.simpletry.core.debug;

import java.util.ArrayDeque;
import java.util.Deque;

public class DebugContext {

    private static final ThreadLocal<Deque<String>> stack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(String method, Object[] args) {

        StringBuilder sb = new StringBuilder();

        sb.append(method).append("(");

        if (args != null) {

            for (int i = 0; i < args.length; i++) {

                sb.append(args[i]);

                if (i < args.length - 1) {
                    sb.append(",");
                }
            }
        }

        sb.append(")");

        stack.get().push(sb.toString());
    }

    public static void pop() {

        Deque<String> deque = stack.get();

        if (!deque.isEmpty()) {
            deque.pop();
        }

        if (deque.isEmpty()) {
            stack.remove();
        }
    }

    public static void dumpTrace(Throwable e) {

        System.err.println("\n===== SimpleTry Debug Trace =====");

        Deque<String> deque = stack.get();

        for (String s : deque) {
            System.err.println(s);
        }

        System.err.println("\nException: " + e.getClass().getSimpleName());

        System.err.println("Message: " + e.getMessage());

        System.err.println("=================================\n");
    }
}