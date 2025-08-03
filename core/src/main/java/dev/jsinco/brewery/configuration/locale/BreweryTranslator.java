package dev.jsinco.brewery.configuration.locale;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.configuration.Config;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class BreweryTranslator extends MiniMessageTranslator {

    Map<Locale, Properties> translations;

    public BreweryTranslator(File localeDirectory) {
        load(localeDirectory);
    }

    public void load(File localeDirectory) {
        if (!localeDirectory.isDirectory()) {
            throw new IllegalArgumentException("Locale directory is not a directory!");
        }
        ImmutableMap.Builder<Locale, Properties> translationsBuilder = new ImmutableMap.Builder<>();
        for (File translationFile : localeDirectory.listFiles(file -> file.getName().endsWith(".lang"))) {
            try (InputStream inputStream = new FileInputStream(translationFile)) {
                Properties translation = new Properties();
                translation.load(inputStream);
                translationsBuilder.put(Locale.of(translationFile.getName().replaceAll(".lang$", "")), translation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.translations = translationsBuilder.build();
        Preconditions.checkArgument(translations.containsKey(Config.config().language()));
    }

    @Override
    public @NotNull Key name() {
        return Key.key("brewery:global_translator");
    }

    @Override
    public @Nullable String getMiniMessageString(@NotNull String key, @NotNull Locale locale) {
        Properties translations = null;
        if (Config.config().clientSideTranslations()) {
            translations = this.translations.get(locale);
        }
        if (translations == null) {
            translations = this.translations.get(Config.config().language());
        }
        Preconditions.checkState(translations != null, "Should have found a translation!");
        return translations.getProperty(key);
    }
}
