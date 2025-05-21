package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.Registry;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BrewingStepPdcType implements PersistentDataType<byte[], BrewingStep> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<BrewingStep> getComplexType() {
        return BrewingStep.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull BrewingStep complex, @NotNull PersistentDataAdapterContext context) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = new DataOutputStream(output)) {
            dataOutputStream.writeUTF(complex.stepType().name());
            switch (complex) {
                case BrewingStep.Age age -> {
                    encodeMoment(age.age(), dataOutputStream);
                    dataOutputStream.writeUTF(age.barrelType().key().toString());
                }
                case BrewingStep.Cook cook -> {
                    encodeMoment(cook.brewTime(), dataOutputStream);
                    encodeIngredients(cook.ingredients(), dataOutputStream);
                    dataOutputStream.writeUTF(cook.cauldronType().key().toString());
                }
                case BrewingStep.Distill distill -> {
                    dataOutputStream.writeInt(distill.runs());
                }
                case BrewingStep.Mix mix -> {
                    encodeMoment(mix.time(), dataOutputStream);
                    encodeIngredients(mix.ingredients(), dataOutputStream);
                }
                default -> throw new IllegalStateException("Unexpected value: " + complex);
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull BrewingStep fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteArrayInputStream input = new ByteArrayInputStream(primitive);
        try (DataInputStream dataInputStream = new DataInputStream(input)) {
            BrewingStep.StepType stepType = BrewingStep.StepType.valueOf(dataInputStream.readUTF());
            return switch (stepType) {
                case COOK -> new CookStepImpl(
                        decodeMoment(dataInputStream),
                        decodeIngredients(dataInputStream),
                        Registry.CAULDRON_TYPE.get(BreweryKey.parse(dataInputStream.readUTF()))
                );
                case DISTILL -> new DistillStepImpl(
                        dataInputStream.readInt()
                );
                case AGE -> new AgeStepImpl(
                        decodeMoment(dataInputStream),
                        Registry.BARREL_TYPE.get(BreweryKey.parse(dataInputStream.readUTF()))
                );
                case MIX -> new MixStepImpl(
                        decodeMoment(dataInputStream),
                        decodeIngredients(dataInputStream)
                );
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void encodeMoment(Moment moment, DataOutputStream dataOutputStream) throws IOException {
        if (moment instanceof Interval(long start, long stop)) {
            dataOutputStream.writeBoolean(false);
            dataOutputStream.writeLong(start);
            dataOutputStream.writeLong(stop);
        } else {
            dataOutputStream.writeBoolean(true);
            dataOutputStream.writeLong(moment.moment());
        }
    }

    private Moment decodeMoment(DataInputStream dataInputStream) throws IOException {
        if (dataInputStream.readBoolean()) {
            return new PassedMoment(dataInputStream.readLong());
        } else {
            return new Interval(dataInputStream.readLong(), dataInputStream.readLong());
        }
    }

    public void encodeIngredients(@NotNull Map<? extends Ingredient, Integer> ingredients, OutputStream outputStream) {
        byte[][] bytesArray = ingredients.entrySet().stream()
                .map(entry -> entry.getKey().getKey() + "/" + entry.getValue())
                .map(string -> string.getBytes(StandardCharsets.UTF_8))
                .toArray(byte[][]::new);
        try {
            DecoderEncoder.encode(bytesArray, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public Map<? extends Ingredient, Integer> decodeIngredients(InputStream inputStream) {
        Map<Ingredient, Integer> ingredients = new HashMap<>();
        byte[][] bytesArray;
        try {
            bytesArray = DecoderEncoder.decode(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(bytesArray)
                .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .map(BukkitIngredientManager.INSTANCE::getIngredientWithAmount)
                .forEach(ingredientAmountPair -> IngredientManager.insertIngredientIntoMap(ingredients, ingredientAmountPair));
        return ingredients;
    }
}
