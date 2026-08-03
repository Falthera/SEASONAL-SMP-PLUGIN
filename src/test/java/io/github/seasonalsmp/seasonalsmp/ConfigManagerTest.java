package io.github.seasonalsmp.seasonalsmp;

import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigManagerTest {

    @Test
    void getBoolean_defaultsToFalseWhenMissing(@TempDir File tempDir) throws IOException {
        File temp = new File(tempDir, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(temp);
        config.set("existing", true);
        config.save(temp);

        SeasonalSMP plugin = Mockito.mock(SeasonalSMP.class);
        ConfigManager manager = new ConfigManager(plugin);
        assertFalse(manager.getBoolean("missing"));
        assertTrue(manager.getBoolean("existing"));
    }

    @Test
    void getString_replacesAmpersandColorCodes(@TempDir File tempDir) throws IOException {
        File temp = new File(tempDir, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(temp);
        config.set("colored", "&aHello &cWorld");
        config.save(temp);

        SeasonalSMP plugin = Mockito.mock(SeasonalSMP.class);
        ConfigManager manager = new ConfigManager(plugin);
        assertEquals("§aHello §cWorld", manager.getString("colored"));
    }

    @Test
    void getString_doesNotOverReplaceOtherAmpersands(@TempDir File tempDir) throws IOException {
        File temp = new File(tempDir, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(temp);
        config.set("text", "a & b");
        config.save(temp);

        SeasonalSMP plugin = Mockito.mock(SeasonalSMP.class);
        ConfigManager manager = new ConfigManager(plugin);
        assertEquals("a & b", manager.getString("text"));
    }
}
