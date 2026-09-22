package dev.jsinco.brewery.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public final class FileUtil {

    public static void extractFile(@NonNull Class<?> clazz, @NonNull String filename, @NonNull Path outDir, boolean replace) {
        try (InputStream in = clazz.getResourceAsStream("/" + filename)) {
            if (in == null) {
                throw new RuntimeException("Could not read file from jar! (" + filename + ")");
            }
            Path path = outDir.resolve(filename);
            if (!Files.exists(path) || replace) {
                Files.createDirectories(path.getParent());
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readInternalResource(String path) {
        try (InputStream inputStream = FileUtil.class.getResourceAsStream(path)) {
            if (inputStream == null) return "";
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement readJsonResource(String path) {
        try (InputStream inputStream = FileUtil.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new FileNotFoundException(path);
            }
            return JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDirectory(String internalResourceName, Path dumpPath) {
        try {
            URL resource = FileUtil.class.getResource(internalResourceName);
            if (resource == null) {
                throw new IllegalArgumentException("Could not find internal resource: '%s'"
                        .formatted(internalResourceName)
                );
            }
            URI uri = resource.toURI();
            if ("jar".equals(uri.getScheme())) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
                    copyResourceTree(fileSystem.getPath(internalResourceName), dumpPath);
                }
            } else {
                copyResourceTree(Path.of(uri), dumpPath);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyResourceTree(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            Iterator<Path> iterator = stream.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                String relative = source.relativize(path).toString();
                Path destination = target.resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Path parent = destination.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
