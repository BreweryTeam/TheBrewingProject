package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.DecoderEncoder;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;

public class BrewingStepPdcType implements PersistentDataType<byte[], BrewingStep> {

    // AES-GCM header constants
    private static final byte[] MAGIC = new byte[] { 'B','R','W','1' };
    private static final int GCM_TAG_BITS = 128; // data authentication
    private static final int VERSION = 2;

    private final boolean useCipher;

    public BrewingStepPdcType(boolean useCipher) {
        this.useCipher = useCipher;
    }

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<BrewingStep> getComplexType() {
        return BrewingStep.class;
    }

    @NotNull
    @Override
    public byte[] toPrimitive(@NotNull BrewingStep complex, @NotNull PersistentDataAdapterContext context) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream headerOut = new DataOutputStream(out);

            if (!useCipher || !Config.config().encryptSensitiveData()) {
                headerOut.write(MAGIC);
                headerOut.writeByte(VERSION);
                headerOut.writeByte(0); // ivLen (0=plaintext)
                try (DataOutputStream dos = new DataOutputStream(out)) {
                    writePayload(complex, dos);
                }
                return out.toByteArray();
            }

            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            headerOut.write(MAGIC);
            headerOut.writeByte(VERSION);
            headerOut.writeByte(iv.length);
            headerOut.write(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, Config.config().encryptionKey(), new GCMParameterSpec(128, iv));

            try (CipherOutputStream cos = new CipherOutputStream(out, cipher);
                 DataOutputStream dos = new DataOutputStream(cos)) {
                writePayload(complex, dos);
            }
            return out.toByteArray();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePayload(@NotNull BrewingStep complex, @NotNull DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(complex.stepType().name());
        switch (complex) {
            case BrewingStep.Age age -> {
                encodeMoment(age.time(), dataOutputStream);
                dataOutputStream.writeUTF(age.barrelType().key().toString());
            }
            case BrewingStep.Cook cook -> {
                encodeMoment(cook.time(), dataOutputStream);
                encodeIngredients(cook.ingredients(), dataOutputStream);
                dataOutputStream.writeUTF(cook.cauldronType().key().toString());
            }
            case BrewingStep.Distill distill -> dataOutputStream.writeInt(distill.runs());
            case BrewingStep.Mix mix -> {
                encodeMoment(mix.time(), dataOutputStream);
                encodeIngredients(mix.ingredients(), dataOutputStream);
            }
            default -> throw new IllegalStateException("Unexpected value: " + complex);
        }
        encodeBrewers(complex.brewers(), dataOutputStream);
    }

    @Override
    public @NotNull BrewingStep fromPrimitive(@NotNull byte[] primitive, @NotNull PersistentDataAdapterContext context) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(primitive);
             DataInputStream headerIn = new DataInputStream(in)) {

            byte[] magic = headerIn.readNBytes(MAGIC.length);

            if (Arrays.equals(magic, MAGIC)) {
                int version = headerIn.readUnsignedByte();
                if (version < 1 || version > VERSION) throw new RuntimeException("Unsupported version: " + version);
                int ivLen = headerIn.readUnsignedByte();
                byte[] iv = headerIn.readNBytes(ivLen);

                if (ivLen == 0) { // brew isn't encrypted
                    try (DataInputStream dis = new DataInputStream(in)) {
                        return readPayload(dis, version);
                    }
                }

                List<SecretKey> knownKeys = new ArrayList<>();
                knownKeys.add(Config.config().encryptionKey());
                knownKeys.addAll(Config.config().previousEncryptionKeys());

                Exception last = null;
                for (SecretKey key : knownKeys) {
                    try {
                        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
                        try (CipherInputStream cis = new CipherInputStream(inDup(primitive, MAGIC.length + 1 + 1 + ivLen), cipher);
                             DataInputStream dis = new DataInputStream(cis)) {
                            return readPayload(dis, version);
                        }
                    } catch (IOException | GeneralSecurityException e) {
                        last = e; // wrong key or tampered data
                    }
                }
                throw new RuntimeException("[AES-GCM] Decryption failed after trying all known keys", last);
            } else {
                return readLegacyDES(primitive);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static ByteArrayInputStream inDup(byte[] all, int offset) {
        return new ByteArrayInputStream(all, offset, all.length - offset);
    }

    private BrewingStep readPayload(@NotNull DataInputStream dataInputStream, int version) throws IOException {
        BrewingStep.StepType stepType = BrewingStep.StepType.valueOf(dataInputStream.readUTF());
        return switch (stepType) {
            case COOK -> new CookStepImpl(
                    decodeMoment(dataInputStream),
                    decodeIngredients(dataInputStream),
                    BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(dataInputStream.readUTF())),
                    decodeBrewers(dataInputStream, version)
            );
            case DISTILL -> new DistillStepImpl(
                    dataInputStream.readInt(),
                    decodeBrewers(dataInputStream, version)
            );
            case AGE -> new AgeStepImpl(
                    decodeMoment(dataInputStream),
                    BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(dataInputStream.readUTF())),
                    decodeBrewers(dataInputStream, version)
            );
            case MIX -> new MixStepImpl(
                    decodeMoment(dataInputStream),
                    decodeIngredients(dataInputStream),
                    decodeBrewers(dataInputStream, version)
            );
        };
    }

    private BrewingStep readLegacyDES(byte[] primitive) {
        Exception lastException;
        try {
            return attemptDecryptDES(primitive, Config.config().encryptionKey()); }
        catch (Exception e) {
            lastException = e;
        }

        for (SecretKey key : Config.config().previousEncryptionKeys()) {
            try {
                return attemptDecryptDES(primitive, key);
            }
            catch (Exception e) {
                lastException = e;
            }
        }
        throw new RuntimeException("[DES] Decryption failed after trying all known keys", lastException);
    }

    private BrewingStep attemptDecryptDES(byte[] primitive, SecretKey key) throws IOException {
        try (
                ByteArrayInputStream input = new ByteArrayInputStream(primitive);
                CipherInputStream cis = new CipherInputStream(input, getLegacyDESCipher(Cipher.DECRYPT_MODE, key));
                DataInputStream dis = new DataInputStream(cis)
        ) {
            return readPayload(dis, 1);
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
                .map(entry -> BukkitIngredientManager.INSTANCE.serializeIngredient(entry.getKey()) + "/" + entry.getValue())
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
                .map(ingredientString -> BukkitIngredientManager.INSTANCE.getIngredientWithAmount(ingredientString, true))
                .forEach(ingredientAmountPair -> IngredientManager.insertIngredientIntoMap(ingredients, ingredientAmountPair.join()));
        return ingredients;
    }

    private void encodeBrewers(@NotNull SequencedSet<UUID> brewers, OutputStream outputStream) throws IOException {
        DecoderEncoder.writeVarInt(brewers.size(), outputStream);
        for (UUID uuid : brewers) {
            outputStream.write(DecoderEncoder.asBytes(uuid));
        }
    }

    private SequencedSet<UUID> decodeBrewers(InputStream inputStream, int version) throws IOException {
        if (version == 1) {
            return new LinkedHashSet<>();
        }
        int length = DecoderEncoder.readVarInt(inputStream);
        SequencedSet<UUID> brewers = new LinkedHashSet<>();
        for (int i = 0; i < length; i++) {
            UUID uuid = DecoderEncoder.asUuid(inputStream.readNBytes(16));
            brewers.add(uuid);
        }
        return brewers;
    }

    private Cipher getLegacyDESCipher(int operationMode, SecretKey key) {
        try {
            Cipher cipher = (Config.config().encryptSensitiveData() && useCipher)
                    ? Cipher.getInstance("DES")
                    : new NullCipher();
            cipher.init(operationMode, key);
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

}