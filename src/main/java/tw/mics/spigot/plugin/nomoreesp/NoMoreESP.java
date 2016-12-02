package tw.mics.spigot.plugin.nomoreesp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.nomoreesp.listener.XRayDetectListener;
import tw.mics.spigot.plugin.nomoreesp.schedule.CheckSchedule;

public class NoMoreESP extends JavaPlugin {
    private static NoMoreESP INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Config.load();
        if(Config.HIDE_ENTITY_ENABLE.getBoolean() || Config.XRAY_DETECT_ENABLE.getBoolean()){
            new CheckSchedule(this);
        }
        
        if(Config.XRAY_DETECT_ENABLE.getBoolean()){
            new XRayDetectListener(this); 
            XRayDetect.initData();
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
    
    public void logDebugInToFile(String msg){
        new Thread(() -> {
            try
            {
                File dataFolder = this.getDataFolder();
                if(!dataFolder.exists()){
                    dataFolder.mkdir();
                }
                File saveTo = new File(dataFolder, "debug.log");
                if (!saveTo.exists()){
                    saveTo.createNewFile();
                }
                FileWriter fw = new FileWriter(saveTo, true);
                PrintWriter pw = new PrintWriter(fw);
                DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ");
                Calendar cal = Calendar.getInstance();
                pw.println(dateFormat.format(cal.getTime()) + msg);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public void logInToFile(String msg){
        new Thread(() -> {
            try
            {
                File dataFolder = this.getDataFolder();
                if(!dataFolder.exists()){
                    dataFolder.mkdir();
                }
                File saveTo = new File(dataFolder, "detect.log");
                if (!saveTo.exists()){
                    saveTo.createNewFile();
                }
                FileWriter fw = new FileWriter(saveTo, true);
                PrintWriter pw = new PrintWriter(fw);
                DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ");
                Calendar cal = Calendar.getInstance();
                pw.println(dateFormat.format(cal.getTime()) + msg);
                pw.flush();
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
