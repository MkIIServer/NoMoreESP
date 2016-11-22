package tw.mics.spigot.plugin.nomoreesp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public enum Config {

    ONLY_PLAYER("only-player", false, "ESP check is only on player?"),
    SEND_FAKE_HEALTH("fake-health", true, "entities health is fake to player (anti health display)"),
    ENABLE_WORLDS("enable-worlds", Arrays.asList("world","world_nether","world_the_end"), "entities on these worlds will be hide");

    private final Object value;
    private final String path;
    private final String description;
    private static YamlConfiguration cfg;
    private static final File f = new File(getPlugin().getDataFolder(), "config.yml");

    private Config(String path, Object val, String description) {
        this.path = path;
        this.value = val;
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return value;
    }

    public boolean getBoolean() {
        return cfg.getBoolean(path);
    }

    public int getInt() {
        return cfg.getInt(path);
    }

    public double getDouble() {
        return cfg.getDouble(path);
    }

    public String getString() {
        return replaceColors(cfg.getString(path));
    }

    public List<String> getStringList() {
        return cfg.getStringList(path);
    }
    
    public void removeStringList(String str) {
        List<String> str_list = getStringList();
        Iterator<String> str_itr = str_list.iterator();
        while(str_itr.hasNext()){
            if(str_itr.next().equals(str)){
                str_itr.remove();
            }
        }
        cfg.set(path, str_list);
        save();
    }

    public static void load() {
        boolean save_flag = false;

        getPlugin().getDataFolder().mkdirs();
        String header = "";
        cfg = YamlConfiguration.loadConfiguration(f);

        for (Config c : values()) {
            header += c.getPath() + ": " + c.getDescription() + System.lineSeparator();
            if (!cfg.contains(c.getPath())) {
                save_flag = true;
                c.set(c.getDefaultValue(), false);
            }
        }
        cfg.options().header(header);

        if (save_flag) {
            save();
            cfg = YamlConfiguration.loadConfiguration(f);
        }
    }

    public static void save() {
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void set(Object value, boolean save) {
        cfg.set(path, value);
        if (save) {
            save();
        }
    }

    private static JavaPlugin getPlugin() {
        return NoMoreESP.getInstance();
    }

    private static String replaceColors(String message) {
        return message.replaceAll("&((?i)[0-9a-fk-or])", "§$1");
    }
}
