package tw.mics.spigot.plugin.nomoreesp;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.nomoreesp.schedule.ESPCheckSchedule;

public class NoMoreESP extends JavaPlugin {
    private static NoMoreESP INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Config.load();
        if(Config.HIDE_ENTITY_ENABLE.getBoolean()){
            new ESPCheckSchedule(this);
        }
        if(Config.FAKE_HEALTH_ENABLE.getBoolean()){
            new HealthHider(this);
        }
    }
    
    @Override
    public void onDisable() {
        this.logDebug("Unregister Listener!");
        HandlerList.unregisterAll();
        this.logDebug("Unregister Schedule tasks!");
        this.getServer().getScheduler().cancelAllTasks();
    }

    public static NoMoreESP getInstance() {
        return INSTANCE;
    }
    
    //log system

    public void log(String str, Object... args) {
        String message = String.format(str, args);
        getLogger().info(message);
    }

    public void logDebug(String str, Object... args) {
        if (Config.DEBUG.getBoolean()) {
            String message = String.format(str, args);
            getLogger().info("(DEBUG) " + message);
        }
    }

}
