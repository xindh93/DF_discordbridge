package dev.dfbridge.util;

public final class GlobMatcher {
    private GlobMatcher() {}

    public static boolean matches(String pattern, String text) {
        // Simple '*' wildcard only
        String[] parts = pattern.split("\\*", -1);
        int pos = 0;
        boolean first = true;
        for (String part : parts) {
            if (part.isEmpty()) {
                first = false;
                continue;
            }
            int index = text.indexOf(part, pos);
            if (index == -1 || (first && !text.startsWith(part))) {
                return false;
            }
            pos = index + part.length();
            first = false;
        }
        if (!pattern.endsWith("*") && !text.endsWith(parts[parts.length - 1])) {
            return false;
        }
        return true;
    }
}
