package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.configuration.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    public static String formatTime(long timeTicks) {
        String format = Config.config().timeFormat();

        long cookingMinuteTicks = Config.config().cauldrons().cookingMinuteTicks();
        long totalMinutes = timeTicks / cookingMinuteTicks;

        long seconds = (timeTicks % cookingMinuteTicks) * 60 / cookingMinuteTicks;
        long minutes = totalMinutes % 60;
        long hours = totalMinutes / 60;

        String result = format;

        result = result.replace("<hours>", String.valueOf(hours))
                .replace("<minutes>", String.valueOf(minutes))
                .replace("<seconds>", String.valueOf(seconds))
                .replace("<ticks>", String.valueOf(timeTicks));

        result = result.replaceAll("(?s)<if-hours>(.*?)</if-hours>", hours > 0 ? "$1" : "")
                .replaceAll("(?s)<if-minutes>(.*?)</if-minutes>", minutes > 0 ? "$1" : "")
                .replaceAll("(?s)<if-seconds>(.*?)</if-seconds>", seconds > 0 ? "$1" : "")
                .replaceAll("(?s)<if-ticks>(.*?)</if-ticks>", timeTicks > 0 ? "$1" : "");

        Pattern pluralPattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pluralPattern.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            int sIndex = matcher.start();
            long value = 2; // default

            Pattern numberPattern = Pattern.compile("(\\d+)");
            Matcher numberMatcher = numberPattern.matcher(result.substring(0, sIndex));
            while (numberMatcher.find()) {
                value = Long.parseLong(numberMatcher.group(1)); // last number before brackets
            }

            String content = matcher.group(1); // content inside the brackets
            String replacement = (value == 1) ? "" : content;
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        result = sb.toString();

        return result;
    }

}
