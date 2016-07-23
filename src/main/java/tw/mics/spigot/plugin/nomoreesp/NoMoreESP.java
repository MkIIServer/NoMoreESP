package tw.mics.spigot.plugin.nomoreesp;

import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.nomoreesp.schedule.ESPCheckSchedule;

public class NoMoreESP extends JavaPlugin {
    private static NoMoreESP INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        new ESPCheckSchedule(this);
        new HealthHider(this);
    }

    public void log(String str, Object... args) {
        String message = String.format(str, args);
        getLogger().info(message);
    }

    public static NoMoreESP getInstance() {
        return INSTANCE;
    }

}
