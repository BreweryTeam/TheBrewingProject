package dev.jsinco.brewery.configuration.serializers;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecretKeySerializer implements ObjectSerializer<SecretKey> {

    @Override
    public boolean supports(@NonNull Class<? super SecretKey> type) {
        return SecretKey.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull SecretKey object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(Base64.getEncoder().encodeToString(object.getEncoded()));
    }

    @Override
    public SecretKey deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String base64 = data.getValue(String.class);
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new SecretKeySpec(bytes, "des");
    }
}
