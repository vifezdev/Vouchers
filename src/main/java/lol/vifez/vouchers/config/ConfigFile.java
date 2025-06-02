package lol.vifez.vouchers.config;

import lol.vifez.vouchers.VouchersPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    private final VouchersPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public ConfigFile(VouchersPlugin plugin, String filename) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        if (config == null) reload();
        return config;
    }

    public void save() {
        try {
            getConfig().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}