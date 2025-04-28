package dev.jsinco.brewery.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.joml.Matrix3d;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class DecoderEncoder {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    private static final byte LIST_ENCODING_VERSION = 0; // In case something needs to change and this plugin has been published

    private DecoderEncoder() {
        throw new IllegalStateException("Utility class");
    }

    public static void encode(byte[][] byteArrayArray, OutputStream outputStream) throws IOException {
        outputStream.write(LIST_ENCODING_VERSION);
        writeVarInt(byteArrayArray.length, outputStream);
        for (byte[] byteArray : byteArrayArray) {
            writeVarInt(byteArray.length, outputStream);
            outputStream.write(byteArray);
        }
    }

    public static byte[][] decode(InputStream inputStream) throws IOException {
        byte version = (byte) inputStream.read();
        if (version != LIST_ENCODING_VERSION) {
            throw new IOException("Invalid format");
        }
        int listSize = readVarInt(inputStream);
        byte[][] output = new byte[listSize][];
        for (int i = 0; i < listSize; i++) {
            int stringLength = readVarInt(inputStream);
            output[i] = inputStream.readNBytes(stringLength);
        }
        return output;
    }

    public static int readVarInt(InputStream inputStream) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = (byte) inputStream.read();
            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;

            if (position >= 32) throw new IOException("VarInt is too big");
        }
        return value;
    }

    public static void writeVarInt(int value, OutputStream outputStream) throws IOException {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                outputStream.write(value);
                return;
            }

            outputStream.write((value & SEGMENT_BITS) | CONTINUE_BIT);

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

    public static String serializeTransformation(Matrix3d matrix3d) {
        double[] doubles = matrix3d.get(new double[9]);
        JsonArray output = new JsonArray();
        for (double aDouble : doubles) {
            output.add(aDouble);
        }
        return output.toString();
    }

    public static Matrix3d deserializeTransformation(String matrixString) {
        JsonArray jsonElement = JsonParser.parseString(matrixString).getAsJsonArray();
        List<JsonElement> jsonElementList = jsonElement.asList();
        double[] m = new double[9];
        for (int i = 0; i < jsonElementList.size(); i++) {
            m[i] = jsonElementList.get(i).getAsDouble();
        }
        return new Matrix3d(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
    }

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
