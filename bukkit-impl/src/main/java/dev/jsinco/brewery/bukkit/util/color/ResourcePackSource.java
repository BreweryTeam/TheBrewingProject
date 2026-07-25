package dev.jsinco.brewery.bukkit.util.color;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.http.HttpStatusCode;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

public interface ResourcePackSource {

    MinecraftResourcePackReader READER = MinecraftResourcePackReader.builder()
            .lenient(true)
            .build();

    ResourcePack readPack() throws IOException, InterruptedException;

    String asString();

    record HttpResourcePackSource(String url, boolean sha256,
                                  @Nullable UUID playerUuid) implements ResourcePackSource {


        @Override
        public ResourcePack readPack() throws IOException, InterruptedException {
            URI uri = URI.create(url);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .headers(
                            HttpHeaderNames.USER_AGENT.toString(), "Minecraft Java/1.21.11",
                            "X-Minecraft-Version", "1.21.11",
                            "X-Minecraft-UUID", (playerUuid == null ? UUID.randomUUID() : playerUuid).toString().replace("-", "")
                    ).GET()
                    .build();
            try (HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build()) {
                HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() != HttpStatusCode.OK) {
                    throw new IOException(String.format("HTTP response %s: %s", response.statusCode(), new String(response.body(), StandardCharsets.UTF_8)));
                }
                if (sha256) {
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                    return READER.readFromInputStream(new ByteArrayInputStream(
                            messageDigest.digest(response.body())
                    ));
                } else {
                    return READER.readFromInputStream(new ByteArrayInputStream(response.body()));
                }
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String asString() {
            return url;
        }
    }

    record PathResourcePackSource(Path file) implements ResourcePackSource {

        @Override
        public ResourcePack readPack() throws IOException, InterruptedException {
            return READER.readFromZipFile(file);
        }

        @Override
        public String asString() {
            return file.toString();
        }
    }

    record FileResourcePackSource(File file) implements ResourcePackSource {

        @Override
        public ResourcePack readPack() throws IOException, InterruptedException {
            if (file.isDirectory()) {
                return READER.readFromDirectory(file);
            } else {
                return READER.readFromZipFile(file);
            }
        }

        @Override
        public String asString() {
            return file.toString();
        }
    }

    record InputStreamResourcePackSource(
            InputStreamSupplier inputStreamSupplier, String alias) implements ResourcePackSource {

        @Override
        public ResourcePack readPack() throws IOException, InterruptedException {
            try (InputStream inputStream = inputStreamSupplier.get()) {
                return READER.readFromInputStream(inputStream);
            }
        }

        @Override
        public String asString() {
            return alias;
        }

    }
}
