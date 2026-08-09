package com.davidblackcn.buildupmenubutton.client.config;

import com.davidblackcn.buildupmenubutton.BuildupMenuButton;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/** Creates and loads the shared Fabric/NeoForge client configuration once during startup. */
public final class LayoutConfigManager {

    private static final String FILE_NAME = BuildupMenuButton.MOD_ID + ".properties";

    private LayoutConfig config;

    public void initialize() {
        if (config == null) {
            config = loadOrCreate();
        }
    }

    public LayoutConfig config() {
        initialize();
        return config;
    }

    /** Updates the live client setting and persists it for the next launch. */
    public void update(LayoutConfig updatedConfig) {
        initialize();
        if (config.equals(updatedConfig)) {
            return;
        }
        config = updatedConfig;
        write(configPath(), updatedConfig);
    }

    private static LayoutConfig loadOrCreate() {
        Path path = configPath();
        if (Files.notExists(path)) {
            write(path, LayoutConfig.DEFAULT);
            return LayoutConfig.DEFAULT;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return LayoutConfigCodec.fromProperties(properties);
        } catch (IOException exception) {
            BuildupMenuButton.LOGGER.warn("[{}] could not read config {}; using defaults", BuildupMenuButton.MOD_ID, path,
                    exception);
            return LayoutConfig.DEFAULT;
        }
    }

    private static Path configPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static void write(Path path, LayoutConfig config) {
        String contents = """
                # Buildup My Old Menu Button client configuration.
                # Changes made through Mod Menu apply immediately.
                # true enables the layout optimization; false keeps the vanilla layout.
                %s=%s
                %s=%s
                """.formatted(
                LayoutConfigCodec.TITLE_SCREEN_LAYOUT_KEY,
                config.titleScreenLayoutEnabled(),
                LayoutConfigCodec.PAUSE_SCREEN_LAYOUT_KEY,
                config.pauseScreenLayoutEnabled());
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, contents, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            BuildupMenuButton.LOGGER.warn("[{}] could not write config {}; live values will not persist", BuildupMenuButton.MOD_ID,
                    path, exception);
        }
    }
}
