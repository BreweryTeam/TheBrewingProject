package dev.jsinco.brewery.effect.text;

import dev.jsinco.brewery.api.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DrunkTextPattern(Pattern pattern, String text, int percentage, int alcohol) implements DrunkTextElement {

    private static final Random RANDOM = new Random();

    @Override
    public @NotNull List<TextTransformation> findTransform(String initial) {
        Matcher matcher = pattern.matcher(initial);
        List<TextTransformation> output = new ArrayList<>();
        while (matcher.find()) {
            if (percentage < RANDOM.nextInt(0, 101)) {
                continue;
            }
            output.add(compileTransformFromMatch(matcher.toMatchResult()));
        }
        return output;
    }

    private TextTransformation compileTransformFromMatch(MatchResult matchResult) {
        String string = matchResult.group();
        boolean isCaps = string.equals(string.toUpperCase(Locale.ROOT)) && !string.equals(string.toLowerCase(Locale.ROOT));
        String text = isCaps ? text().toUpperCase(Locale.ROOT) : text().toLowerCase(Locale.ROOT);
        if (matchResult.groupCount() > 0) {
            Pair<Integer, Integer> range = findRange(matchResult);
            int start = matchResult.start();
            return new TextTransformation(text, range.first() + start, range.second() + start, alcohol());
        }
        return new TextTransformation(text, matchResult.start(), matchResult.end(), alcohol());
    }

    private Pair<Integer, Integer> findRange(MatchResult matchResult) {
        int matchStart = matchResult.start();
        int start = 0;
        int end = matchResult.end() - matchStart;
        for (int i = 1; i < matchResult.groupCount() + 1; i++) {
            int groupEnd = matchResult.end(i) - matchStart;
            int groupStart = matchResult.start(i) - matchStart;
            if (groupStart <= start) {
                start = groupEnd;
            } else if (groupEnd >= end) {
                end = groupStart;
            }
        }
        return new Pair<>(start, end);
    }
}
