package dev.dfbridge.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageFormatter {
    private MessageFormatter() {}

    private static final Pattern ROLE_PATTERN = Pattern.compile("\\{role:([^}]+)\\}");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    public static String render(String template, Map<String, String> data, ConfigurationSection rolesSection) {
        if (template == null) template = "";
        String result = template;

        // {role:KEY} -> <@&ROLEID>
        Matcher rm = ROLE_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (rm.find()) {
            String key = rm.group(1);
            String roleId = rolesSection != null ? rolesSection.getString(key) : null;
            String replacement = roleId != null && !roleId.isEmpty() ? "<@&" + roleId + ">" : "";
            rm.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        rm.appendTail(sb);
        result = sb.toString();

        // {everyone} -> @everyone
        result = result.replace("{everyone}", "@everyone");

        // {var} replacements
        Matcher vm = VAR_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (vm.find()) {
            String key = vm.group(1);
            String replacement = data != null ? data.getOrDefault(key, "") : "";
            vm.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        vm.appendTail(sb);
        result = sb.toString();

        return result;
    }

    /**
     * Applies automatic @everyone prefix when the given type matches any of the provided glob patterns.
     * If the message already contains @everyone at the start, no additional prefix is added.
     *
     * @param type     event type (e.g., "rift.unstable")
     * @param message  formatted message to possibly prefix
     * @param patterns list of glob patterns
     * @return message with auto prefix applied if matched
     */
    public static String applyAutoPrefixForGroups(String type, String message, List<String> patterns) {
        if (message == null || patterns == null) {
            return message;
        }
        for (String pattern : patterns) {
            if (GlobMatcher.matches(pattern, type)) {
                if (!message.startsWith("@everyone")) {
                    return "@everyone " + message;
                }
                return message;
            }
        }
        return message;
    }
}
