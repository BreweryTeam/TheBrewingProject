 /*
  * This file comes from granny/Pl3xMap and is licensed under the MIT License.
  * Source: https://github.com/granny/Pl3xMap/blob/v3/core/src/main/java/net/pl3x/map/core/configuration/AbstractConfig.java
  */
 package dev.jsinco.brewery.configuration;

 import dev.jsinco.brewery.util.Logging;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.simpleyaml.configuration.ConfigurationSection;
 import org.simpleyaml.configuration.comments.CommentType;
 import org.simpleyaml.configuration.file.YamlConfiguration;
 import org.simpleyaml.configuration.file.YamlFile;
 import org.simpleyaml.exceptions.InvalidConfigurationException;

 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.nio.file.Path;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.Map;

 public abstract class AbstractConfig {
     private YamlFile config;
     private YamlConfiguration backup;

     public @NotNull YamlFile getConfig() {
         return this.config;
     }

     protected void reload(@NotNull Path path, String defaultLocation, @NotNull Class<? extends @NotNull AbstractConfig> clazz) {
         // read yaml from file
         this.config = new YamlFile(path.toFile());
         try (InputStream inputStream = AbstractConfig.class.getResourceAsStream("/" + defaultLocation)) {
             this.backup = YamlConfiguration.loadConfiguration(inputStream);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         try {
             getConfig().createOrLoadWithComments();
         } catch (InvalidConfigurationException e) {
             Logging.error("Could not load " + path.getFileName() + ", please correct your syntax errors");
             throw new RuntimeException(e);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         @Nullable Header header = clazz.getDeclaredAnnotation(Header.class);
         if (header != null) {
             config.setHeader(header.value());
         }
         // load data from yaml
         Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
             Key key = field.getDeclaredAnnotation(Key.class);
             Comment comment = field.getDeclaredAnnotation(Comment.class);
             if (key == null) {
                 return;
             }
             try {
                 Object value = getValue(key.value(), field.get(null));
                 field.set(null, value);
                 if (comment != null) {
                     setComment(key.value(), comment.value());
                 }
             } catch (Throwable e) {
                 Logging.error("Failed to load " + key.value() + " from " + path.getFileName().toString());
                 e.printStackTrace();
             }
         });

         save();
     }

     protected void save() {
         // save yaml to disk
         try {
             getConfig().save();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }

     protected @Nullable Object getValue(@NotNull String path, @Nullable Object def) {
         if (def == null) {
             def = backup.get(path);
         }
         if (getConfig().get(path) == null) {
             set(path, def);
         }
         return get(path, def);
     }

     protected void setComment(@NotNull String path, @Nullable String comment) {
         getConfig().setComment(path, comment, CommentType.BLOCK);
     }

     protected @Nullable Object get(@NotNull String path, @Nullable Object def) {
         Object val = get(path);
         return val == null ? def : val;
     }

     protected @Nullable Object get(@NotNull String path) {
         Object value = getConfig().get(path);
         if (!(value instanceof ConfigurationSection section)) {
             return value;
         }
         Map<String, Object> map = new LinkedHashMap<>();
         for (String key : section.getKeys(false)) {
             Object rawValue = get(path + "." + key);
             if (rawValue == null) {
                 continue;
             }
             map.put(key, rawValue);
         }
         return map;
     }

     protected void set(@NotNull String path, @Nullable Object value) {
         getConfig().set(path, value);
     }

     @Target(ElementType.FIELD)
     @Retention(RetentionPolicy.RUNTIME)
     public @interface Key {
         @NotNull String value();
     }

     @Target(ElementType.FIELD)
     @Retention(RetentionPolicy.RUNTIME)
     public @interface Comment {
         @NotNull String value();
     }

     @Target(ElementType.TYPE)
     @Retention(RetentionPolicy.RUNTIME)
     public @interface Header {
         @NotNull String value();
     }
 }