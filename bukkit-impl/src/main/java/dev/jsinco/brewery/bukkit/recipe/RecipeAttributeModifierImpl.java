package dev.jsinco.brewery.bukkit.recipe;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public record RecipeAttributeModifierImpl(@NonNull Attribute attribute, AttributeModifier.@NonNull Operation operation,
                                          @NonNull DoubleRange amountRange, @NonNull Interval durationRange,
                                          @NonNull NamespacedKey key) {

    private static final Map<String, AttributeModifier.Operation> OPERATION_ALIASES = Map.ofEntries(

            Map.entry("add_number", AttributeModifier.Operation.ADD_NUMBER),
            Map.entry("add_value", AttributeModifier.Operation.ADD_NUMBER),
            Map.entry("addition", AttributeModifier.Operation.ADD_NUMBER),
            Map.entry("add", AttributeModifier.Operation.ADD_NUMBER),
            Map.entry("value", AttributeModifier.Operation.ADD_NUMBER),
            Map.entry("0", AttributeModifier.Operation.ADD_NUMBER),

            Map.entry("add_scalar", AttributeModifier.Operation.ADD_SCALAR),
            Map.entry("add_multiplied_base", AttributeModifier.Operation.ADD_SCALAR),
            Map.entry("multiply_base", AttributeModifier.Operation.ADD_SCALAR),
            Map.entry("base", AttributeModifier.Operation.ADD_SCALAR),
            Map.entry("1", AttributeModifier.Operation.ADD_SCALAR),

            Map.entry("multiply_scalar_1", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("multiply_scalar", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("add_scalar_1", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("add_multiplied_total", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("multiply_total", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("multiply", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("total", AttributeModifier.Operation.MULTIPLY_SCALAR_1),
            Map.entry("2", AttributeModifier.Operation.MULTIPLY_SCALAR_1)
    );

    public double randomAmount() {
        return amountRange.random();
    }

    public long randomDuration() {
        return ThreadLocalRandom.current().nextLong(durationRange.start(), durationRange.stop() + 1);
    }

    public static NamespacedKey defaultKey(@NonNull String recipeId, Attribute attribute, AttributeModifier.Operation operation) {
        return TheBrewingProject.key("brew/" + sanitizeKeyPart(recipeId)
                + "/" + attribute.getKey().getNamespace() + "/" + attribute.getKey().getKey()
                + "/" + operation.name().toLowerCase(Locale.ROOT));
    }

    public static @NonNull NamespacedKey parseKey(@NonNull String string) {
        String normalized = string.trim().toLowerCase(Locale.ROOT);
        Preconditions.checkArgument(!normalized.isEmpty(), "Attribute modifier key can not be empty");
        NamespacedKey key = normalized.contains(":") ? NamespacedKey.fromString(normalized) : TheBrewingProject.key(normalized);
        Preconditions.checkArgument(key != null, "Invalid attribute modifier key: " + string);
        return key;
    }

    private static String sanitizeKeyPart(String string) {
        return string.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
    }

    public Component displayName() {
        return Component.translatable(attribute.translationKey())
                .append(Component.text(" "))
                .append(Component.text(operationName(operation)))
                .append(Component.text(" "))
                .append(Component.text(amountRange.asDisplayString()))
                .append(Component.text(" "))
                .append(durationRange.displayName())
                .append(Component.text("t"));
    }

    // <attribute>/<amount>/<duration in seconds>[/<operation>[/<key>]]
    public static RecipeAttributeModifierImpl parseConfig(@NonNull String string, @NonNull String recipeId, @Nullable String context) throws IllegalArgumentException {
        String[] parts = string.trim().split("/");
        Preconditions.checkArgument(parts.length >= 3 && parts.length <= 5,
                "Expected attribute modifier with format <attribute>/<amount>/<duration>[/<operation>[/<key>]], got: " + string);
        Attribute attribute = parseAttribute(parts[0]);
        DoubleRange amount = DoubleRange.parse(parts[1], context);
        if (amount.max < amount.min) {

        }
        Interval duration = Interval.parseString(parts[2].trim()).multiply(Moment.SECOND);
        Preconditions.checkArgument(duration.start() >= 0 && duration.stop() >= duration.start(),
                "Invalid duration for attribute modifier: " + parts[2]);
        AttributeModifier.Operation operation = parts.length >= 4 ? parseOperation(parts[3]) : AttributeModifier.Operation.ADD_NUMBER;
        NamespacedKey key = parts.length == 5 ? parseKey(parts[4]) : defaultKey(recipeId, attribute, operation);
        return new RecipeAttributeModifierImpl(attribute, operation, amount, duration, key);
    }

    public static @NonNull Attribute parseAttribute(@NonNull String string) {
        String normalized = string.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        Attribute attribute = lookupAttribute(normalized);
        if (attribute != null) {
            return attribute;
        }
        String namespace = "minecraft";
        String value = normalized;
        int colonIndex = normalized.indexOf(':');
        if (colonIndex >= 0) {
            namespace = normalized.substring(0, colonIndex);
            value = normalized.substring(colonIndex + 1);
        }
        for (String prefix : new String[]{"generic.", "player.", "zombie.", "horse.", "generic_", "player_", "zombie_", "horse_"}) {
            if (value.startsWith(prefix)) {
                attribute = lookupAttribute(namespace + ":" + value.substring(prefix.length()));
                if (attribute != null) {
                    return attribute;
                }
            }
        }
        throw new IllegalArgumentException("Unknown attribute: " + string);
    }

    private static @Nullable Attribute lookupAttribute(String key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        return namespacedKey == null ? null : Registry.ATTRIBUTE.get(namespacedKey);
    }

    public static AttributeModifier.@NonNull Operation parseOperation(@NonNull String string) {
        String normalized = string.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        int colonIndex = normalized.indexOf(':');
        if (colonIndex >= 0) {
            normalized = normalized.substring(colonIndex + 1);
        }
        AttributeModifier.Operation operation = OPERATION_ALIASES.get(normalized);
        Preconditions.checkArgument(operation != null, "Unknown attribute modifier operation: " + string
                + ", expected one of add_value, add_multiplied_base, add_multiplied_total");
        return operation;
    }

    public static String operationName(AttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_NUMBER -> "add_value";
            case ADD_SCALAR -> "add_multiplied_base";
            case MULTIPLY_SCALAR_1 -> "add_multiplied_total";
        };
    }

    @NullMarked
    public record DoubleRange(double min, double max) {

        public DoubleRange {
            Preconditions.checkArgument(Double.isFinite(min) && Double.isFinite(max), "Amount has to be a finite number");
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }
        }

        // e.g. -0.08;-0.04
        public static DoubleRange parse(String string, @Nullable String context) {
            String trimmed = string.trim();
            if (trimmed.contains(";")) {
                String[] split = trimmed.split(";");
                Preconditions.checkArgument(split.length == 2, "Expected range with format <min>;<max>, got: " + string);
                double min = Double.parseDouble(split[0].trim());
                double max = Double.parseDouble(split[1].trim());
                if (min > max && context != null) {
                    Logger.logWarn("Invalid double range for amount in attribute-modifier at '%s': Expected a min value at the start of range definition".formatted(context));
                }
                return new DoubleRange(min, max);
            }
            double value = Double.parseDouble(trimmed);
            return new DoubleRange(value, value);
        }

        public double random() {
            if (min == max) {
                return min;
            }
            return ThreadLocalRandom.current().nextDouble(min, max);
        }

        public String asString() {
            if (min == max) {
                return Double.toString(min);
            }
            return min + ";" + max;
        }

        public String asDisplayString() {
            if (min == max) {
                return Double.toString(min);
            }
            return "[" + min + ", " + max + "]";
        }
    }
}
