package de.felix.permissions.message;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Messages {

    private final Map<String, String> cache = new HashMap<>();
    private final JavaPlugin plugin;

    public Messages(FileConfiguration config) {
        this(config, null);
    }

    public Messages(FileConfiguration config, JavaPlugin plugin) {
        this.plugin = plugin;
        loadLanguage(config.getString("language", "en"), config);
    }

    private void loadLanguage(String lang, FileConfiguration config) {
        if (plugin != null) {
            File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
            if (langFile.exists()) {
                loadFromConfig(YamlConfiguration.loadConfiguration(langFile));
                return;
            }

            InputStream stream = plugin.getResource("lang/" + lang + ".yml");
            if (stream != null) {
                loadFromConfig(
                        YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8)));
                return;
            }
        }

        var section = config.getConfigurationSection("messages");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                cache.put(key, section.getString(key, ""));
            }
        } else {
            cache.put("no-permission", "&cNo permission: {permission}");
            cache.put("group-not-found", "&cGroup not found: {group}");
        }
    }

    private void loadFromConfig(FileConfiguration langConfig) {
        for (String key : langConfig.getKeys(false)) {
            cache.put(key, langConfig.getString(key, ""));
        }
    }

    public String get(String key, String... placeholders) {
        String msg = cache.getOrDefault(key, "&cMissing: " + key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            msg = msg.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return msg.replace('&', '§');
    }

    public String getRaw(String key) {
        return cache.getOrDefault(key, "&cMissing: " + key).replace('&', '§');
    }
}
