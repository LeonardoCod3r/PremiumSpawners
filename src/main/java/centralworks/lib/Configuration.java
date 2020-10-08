package centralworks.lib;

import centralworks.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Configuration {

    private final String fileName;
    private final YamlConfiguration config;

    public Configuration(String fileName) {
        this.fileName = fileName;
        if (!new File(Main.getInstance().getDataFolder(), this.fileName + ".yml").exists()) {
            Main.getInstance().saveResource(this.fileName + ".yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(new File(Main.getInstance().getDataFolder() + File.separator + this.fileName + ".yml"));
    }

    public void save() {
        if (getFile().exists()) {
            try {
                config.save(getFile());
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("§cOcorreu um erro no salvamento do arquivo: " + getFile().getName());
            }
        }
    }

    public String get(String path, Boolean color) {
        if (!config.contains(path)) {
            return "";
        }
        if (color) {
            return ChatColor.translateAlternateColorCodes('&', config.getString(path));
        }
        return config.getString(path);
    }

    public String getMessage(String message) {
        return get("Messages." + message, true);
    }

    public List<String> getList(String path, Boolean color) {
        if (color) {
            return config.getStringList(path).stream().map(s -> s.replaceAll("&", "§")).collect(Collectors.toList());
        }
        return config.getStringList(path);
    }

    public Set<String> section(String path) {
        return config.getConfigurationSection(path).getKeys(false);
    }

    public Boolean is(String path) {
        return config.getBoolean(path);
    }

    public Integer getInt(String path) {
        if (has(path)) return config.getInt(path);
        else return 0;
    }

    public Double getDouble(String path) {
        if (!has(path)) return 0D;
        return config.getDouble(path);
    }

    public Long getLong(String path) {
        if (!has(path)) return 0L;
        return config.getLong(path);
    }

    public Float getFloat(String path) {
        return Float.valueOf(getObject(path).toString());
    }

    public Object getObject(String path) {
        return config.get(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public Boolean has(String path) {
        return getObject(path) != null;
    }

    public void saveLocationInConfig(String pathName, Location location) {
        final String path = "Locations." + pathName + ".";
        set(path + "world", location.getWorld().getName());
        set(path + "x", location.getX());
        set(path + "y", location.getY());
        set(path + "z", location.getZ());
        set(path + "yaw", String.valueOf(location.getYaw()));
        set(path + "pitch", String.valueOf(location.getPitch()));
        save();
    }

    public String getFileName() {
        return fileName;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return new File(Main.getInstance().getDataFolder(), this.fileName + ".yml");
    }

}
