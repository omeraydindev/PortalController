package ma.portal.controller;

// temp
public class Logs {
    private static final StringBuilder logs = new StringBuilder();

    public static void clear() {
        logs.setLength(0);
    }

    public static void log(String s) {
        logs.append("> ");
        logs.append(s);
        logs.append('\n');
    }

    public static String get() {
        return logs.toString();
    }
}
