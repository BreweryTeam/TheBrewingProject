package dev.jsinco.brewery.api.util;

import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    @ApiStatus.Internal
    public static List<String> complexSplit(String string) {
        List<String> output = new ArrayList<>();
        StringBuilder metaElementBuilder = new StringBuilder();
        boolean inQuotes = false;
        boolean escaping = false;
        int curlyBracketsDepth = 0;
        for (char character : string.toCharArray()) {
            if (character == '\\' && inQuotes) {
                escaping = true;
                continue;
            }
            if (!escaping && character == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && character == '{') {
                curlyBracketsDepth++;
            }
            if (!inQuotes && character == '}') {
                curlyBracketsDepth--;
            }
            if (curlyBracketsDepth < 0) {
                throw new IllegalArgumentException("Invalid syntax, expected leading curly brace");
            }
            escaping = false;
            if (!inQuotes && curlyBracketsDepth == 0 && character == ',') {
                output.add(metaElementBuilder.toString());
                metaElementBuilder = new StringBuilder();
                continue;
            }
            metaElementBuilder.append(character);
        }
        if (!metaElementBuilder.isEmpty()) {
            output.add(metaElementBuilder.toString());
        }
        return output;
    }

    public static MetaData parseMeta(String string) {
        MetaData metaData = new MetaData();
        for (String metaElementString : StringUtil.complexSplit(string)) {
            String[] split = metaElementString.split("=", 2);
            if (split.length != 2) {
                continue;
            }
            Key metaKey = KeyUtil.brewery(split[0].strip());
            String valueString = split[1].strip();
            metaData = metaData.withMeta(metaKey, MetaDataType.STRING,
                    isInQuotes(valueString) ? valueString.substring(1, valueString.length() - 1) : valueString
            );
        }
        return metaData;
    }

    public static boolean isInQuotes(String string) {
        return string.startsWith("\"") && string.endsWith("\"");
    }
}
