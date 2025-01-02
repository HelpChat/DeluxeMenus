package com.extendedclip.deluxemenus.utils;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;

public final class DumpUtils {
    @NotNull
    public static final String URL = "https://paste.helpch.at/";
    @NotNull
    private static final Gson gson = new Gson();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.LONG)
        .withLocale(Locale.US)
        .withZone(ZoneId.of("UTC"));

    private DumpUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    @NotNull
    public static CompletableFuture<String> postDump(@NotNull final String dump) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpURLConnection connection = ((HttpURLConnection) new URL(URL + "documents")
                    .openConnection());
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                connection.setDoOutput(true);

                connection.connect();

                try (final OutputStream stream = connection.getOutputStream()) {
                    stream.write(dump.getBytes(StandardCharsets.UTF_8));
                }

                try (final InputStream stream = connection.getInputStream()) {
                    final String json = CharStreams.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    return gson.fromJson(json, JsonObject.class).get("key").getAsString();
                }
            } catch (final IOException ex) {
                throw new CompletionException(ex);
            }
        });
    }

    @NotNull
    public static String createDump(
        @NotNull final DeluxeMenus plugin,
        @NotNull final String name
    ) throws RuntimeException {
        final var builder = new StringBuilder();

        builder.append("Generated On: ")
            .append(DATE_FORMAT.format(Instant.now()))
            .append(System.lineSeparator())
            .append(System.lineSeparator());

        builder.append("DeluxeMenus Version: ")
            .append(plugin.getDescription().getVersion())
            .append(System.lineSeparator());

        builder.append("Java Version: ")
            .append(System.getProperty("java.version"))
            .append(System.lineSeparator());

        builder.append("Server Info:")
            .append(plugin.getServer().getBukkitVersion())
            .append('/')
            .append(plugin.getServer().getVersion())
            .append(System.lineSeparator())
            .append(System.lineSeparator());

        if (name.equalsIgnoreCase("config")) {
            if (createConfigDump(plugin, builder)) {
                return builder.toString();
            }

            throw new RuntimeException("Something went wrong while creating the config dump");
        }

        if (createMenuDump(plugin, name, builder)) {
            return builder.toString();
        }

        throw new RuntimeException("Something went wrong while creating the menu dump");
    }

    private static boolean createMenuDump(
        @NotNull final DeluxeMenus plugin,
        @NotNull final String menuName,
        @NotNull final StringBuilder builder
    ) {
        builder.append("Menu Name: ")
            .append(menuName)
            .append(System.lineSeparator());

        final var config = plugin.getConfig();
        final var guiMenus = config.getConfigurationSection("gui_menus");

        if (guiMenus == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "No gui_menus section found in config.yml!"
            );

            return false;
        }

        final Set<String> keys = guiMenus.getKeys(false);

        if (!keys.contains(menuName)) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "File for the " + menuName + " menu is not declared in config.yml!"
            );

            return false;
        }

        final String fileName = plugin.getConfig().getString("gui_menus." + menuName + ".file");

        if (fileName == null) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "File for the " + menuName + " menu is not declared in config.yml!"
            );

            return false;
        }

        if (!fileName.endsWith(".yml")) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "File for the " + menuName + " menu is not declared in config.yml!"
            );

            return false;
        }

        builder.append("Menu Path: ")
            .append(fileName)
            .append(System.lineSeparator())
            .append(System.lineSeparator())
            .append("---------------------------------------------")
            .append(System.lineSeparator())
            .append(System.lineSeparator());

        final var menuFile = new File(plugin.getConfiguration().getMenuDirector(), fileName);

        if (!menuFile.exists() || !menuFile.isFile()) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Could not find the " + fileName + " file in " +
                    plugin.getConfiguration().getMenuDirector().getPath() + " while creating the dump!"
            );

            return false;
        }

        try {
            Files.readAllLines(menuFile.toPath(), StandardCharsets.UTF_8).forEach(line ->
                builder.append(line).append(System.lineSeparator())
            );
        } catch (final IOException exception) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Something went wrong while reading the the file: " + fileName
            );

            plugin.printStacktrace(
                "Something went wrong while reading the the file: " + fileName,
                exception
            );
            return false;
        }

        return true;
    }

    private static boolean createConfigDump(
        @NotNull final DeluxeMenus plugin,
        @NotNull final StringBuilder builder
    ) {
        final File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists() || !configFile.isFile()) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Could not find the " + configFile + " file in " + plugin.getDataFolder().getPath()
                    + " while creating the dump!"
            );

            return false;
        }

        builder.append("---------------------------------------------")
            .append(System.lineSeparator())
            .append(System.lineSeparator());

        try {
            Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8).forEach(line ->
                builder.append(line).append(System.lineSeparator())
            );
        } catch (final IOException exception) {
            plugin.debug(
                DebugLevel.HIGHEST,
                Level.WARNING,
                "Something went wrong while reading the the file: " + configFile
            );

            plugin.printStacktrace(
                "Something went wrong while reading the the file: " + configFile,
                exception
            );
            return false;
        }

        return true;
    }
}
