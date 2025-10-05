package dev.jsinco.brewery.configuration.serializers;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class SecretKeySerializer implements ObjectSerializer<SecretKey> {

    @Override
    public boolean supports(@NonNull Class<? super SecretKey> type) {
        return SecretKey.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull SecretKey key, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        byte[] raw = key.getEncoded();
        if (raw == null) throw new IllegalArgumentException("SecretKey#getEncoded returned null");
        data.setValue(Base64.getEncoder().encodeToString(raw));
    }

    @Override
    public SecretKey deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String token = data.getValue(String.class);
        if (token == null || token.isEmpty()) return null;
        String base64 = token.trim();

        final byte[] raw;
        try {
            raw = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 for SecretKey", e);
        }

        try {
            switch (raw.length) {
                case 8 -> {
                    DESKeySpec spec = new DESKeySpec(raw);
                    return SecretKeyFactory.getInstance("DES").generateSecret(spec);
                }
                case 16, 24, 32 -> { // AES-128/192/256
                    return new SecretKeySpec(raw, "AES");
                }
                default -> throw new IllegalArgumentException("Unsupported key length: " + raw.length
                        + " (expected 8b for DES or 16b/24b/32b for AES)");
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Failed to rebuild SecretKey: " + e.getMessage(), e);
        }
    }
}
