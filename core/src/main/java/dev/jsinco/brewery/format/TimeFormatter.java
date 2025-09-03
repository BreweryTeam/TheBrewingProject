package dev.jsinco.brewery.format;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeFormatter {

    public static String format(long timeTicks, TimeFormat format) {
        return  format(timeTicks, format, TimeModifier.NORMAL);
    }

    public static String format(long timeTicks, TimeFormat format, TimeModifier modifier) {

        Map<String, Long> placeholders = new LinkedHashMap<>();
        double tpm = modifier.getTicksPerMinute();

        long totalMinutes = (long) Math.floor(timeTicks / tpm);
        placeholders.put("minutes-total", totalMinutes);
        long totalSeconds = (long) Math.floor(timeTicks * 60.0 / tpm);
        placeholders.put("seconds-total", totalSeconds);

        long totalHours = totalMinutes / 60;
        placeholders.put("hours-total", totalHours);
        long totalDays = totalHours / 24;
        placeholders.put("days-total", totalDays);
        long totalYears = totalDays / 365;
        placeholders.put("years-total", totalYears);
        long totalDecades = totalYears / 10;
        placeholders.put("decades-total", totalDecades);

        long minutes = totalMinutes % 60;
        placeholders.put("minutes", minutes);
        long hours = totalHours % 24;
        placeholders.put("hours", hours);
        long days = totalDays % 365;
        placeholders.put("days", days);
        long years = totalYears % 10;
        placeholders.put("years", years);

        long ticks = (long) (timeTicks - Math.floor(totalMinutes * tpm));
        placeholders.put("ticks", ticks);
        long seconds = (long) Math.floor((timeTicks - Math.floor(totalMinutes * tpm)) * 60.0 / tpm);
        placeholders.put("seconds", seconds);

        placeholders.put("ticks-total", timeTicks);
        placeholders.put("decades", totalDecades);

        String result = format.get();

        for (Map.Entry<String, Long> entry : placeholders.entrySet()) {
            result = result.replace("<" + entry.getKey() + ">", String.valueOf(entry.getValue()));
        }

        for (Map.Entry<String, Long> entry : placeholders.entrySet()) {

            boolean condition = entry.getValue() > 0;
            String name = entry.getKey();

            result = result.replaceAll("(?s)<if-" + Pattern.quote(name) + ">(.*?)</if-" + Pattern.quote(name) + ">", condition ? "$1" : "");
            result = result.replaceAll("(?s)<if-!" + Pattern.quote(name) + ">(.*?)</if-!" + Pattern.quote(name) + ">", condition ? "" : "$1");
        }

        Pattern[] blockPatterns = new Pattern[] { // Don't even try to understand this madness, it's not worth your time
            Pattern.compile("\\[\\[\\[(.*?)\\]\\]\\](?:<<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>>|<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>|<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>)?"
                + "|\\{\\{\\{(.*?)\\}\\}\\}(?:<<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>>|<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>|<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>)?", Pattern.DOTALL),
            Pattern.compile("\\[\\[(.*?)\\]\\](?:<<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>>|<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>|<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>)?"
                + "|\\{\\{(.*?)\\}\\}(?:<<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>>|<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>|<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>)?", Pattern.DOTALL),
            Pattern.compile("\\[(.*?)](?:<<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>>|<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>|<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>)?"
                + "|\\{(.*?)}(?:<<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>>|<<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>>|<\\s*(\\d+(?:\\s*-\\s*\\d+)?)\\s*>)?", Pattern.DOTALL)
        };

        for (Pattern blockPattern : blockPatterns) {

            Matcher matcher = blockPattern.matcher(result);
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                int blockStart = matcher.start();

                String prefix = result.substring(0, blockStart);
                StringBuilder outside = new StringBuilder(prefix.length());
                boolean inSquare = false, inCurly = false, inAngle = false;

                // Get everything outside of pluralization blocks
                for (int i = 0; i < prefix.length(); i++) {
                    char c = prefix.charAt(i);

                    if (!inCurly && !inAngle && c == '[') { inSquare = true; outside.append(' '); continue; }
                    if (inSquare && c == ']') { inSquare = false; outside.append(' '); continue; }

                    if (!inSquare && !inAngle && c == '{') { inCurly = true; outside.append(' '); continue; }
                    if (inCurly && c == '}') { inCurly = false; outside.append(' '); continue; }

                    if (!inSquare && !inCurly && c == '<') { inAngle = true; outside.append(' '); continue; }
                    if (inAngle && c == '>') { inAngle = false; outside.append(' '); continue; }

                    if (inSquare || inCurly || inAngle) { outside.append(' '); continue; }
                    outside.append(c);
                }

                // Find the last prior number that is outside pluralization blocks
                Matcher num = Pattern.compile("(\\d+)").matcher(outside);
                long value = 0; // default value if no prior number found
                while (num.find()) value = Long.parseLong(num.group(1));

                boolean isCurly = matcher.group(5) != null;
                String content, spec = null;
                int mode = 0; // 0 = whole number (<…> or none), 1 = last digit (<<…>>), 2 = last two digits (<<<…>>>)

                if (isCurly) {
                    content = matcher.group(5);
                    if (matcher.group(6) != null) { spec = matcher.group(6); mode = 2; }
                    else if (matcher.group(7) != null) { spec = matcher.group(7); mode = 1; }
                    else if (matcher.group(8) != null) { spec = matcher.group(8); mode = 0; }
                } else {
                    content = matcher.group(1);
                    if (matcher.group(2) != null) { spec = matcher.group(2); mode = 2; }
                    else if (matcher.group(3) != null) { spec = matcher.group(3); mode = 1; }
                    else if (matcher.group(4) != null) { spec = matcher.group(4); mode = 0; }
                }

                long compareValue = value; // whole number
                if (mode == 1) compareValue = value % 10; // last digit
                else if (mode == 2) compareValue = value % 100; // last two digits

                boolean matches;
                if (spec == null) {
                    matches = (compareValue == 1); // default when no <>/<<>>/<<<>>> provided
                } else {
                    String str = spec.replaceAll("\\s+", ""); // allow spaces like "2 - 4"
                    int dash = str.indexOf('-');
                    if (dash >= 0) {
                        long a = Long.parseLong(str.substring(0, dash));
                        long b = Long.parseLong(str.substring(dash + 1));
                        if (a > b) { long temp = a; a = b; b = temp; } // normalize reversed ranges
                        matches = (compareValue >= a && compareValue <= b);
                    } else {
                        long n = Long.parseLong(str);
                        matches = (compareValue == n);
                    }
                }

                String replacement = (isCurly == matches) ? content : "";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }

            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

}
