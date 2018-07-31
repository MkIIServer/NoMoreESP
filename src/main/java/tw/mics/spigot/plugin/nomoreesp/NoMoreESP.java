package tw.mics.spigot.plugin.nomoreesp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.nomoreesp.listener.XRayDetectListener;
import tw.mics.spigot.plugin.nomoreesp.schedule.CheckSchedule;

public class NoMoreESP extends JavaPlugin {
    private static NoMoreESP INSTANCE;
    private CheckSchedule checkschedule;
    private File logFolder;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Config.load();
        if(Config.HIDE_ENTITY_ENABLE.getBoolean() || Config.XRAY_DETECT_ENABLE.getBoolean()){
            checkschedule = new CheckSchedule(this);
        }
        
        if(Config.XRAY_DETECT_ENABLE.getBoolean()){
            new XRayDetectListener(this); 
            XRayDetect.initData();
        }

        //reload support
        Iterator<? extends Player> itr = Bukkit.getOnlinePlayers().iterator();
        while(itr.hasNext()){
            XRayDetect.checkUUIDDataExist(itr.next().getUniqueId());
        }

        //create log folder
        logFolder = new File(NoMoreESP.getInstance().getDataFolder(), "logs");
        logFolder.mkdirs();
    }
    
    @Override
    public void onDisable() {
        this.logDebug("Unregister Listener!");
        HandlerList.unregisterAll();
        this.logDebug("Unregister Schedule tasks!");
        this.getServer().getScheduler().cancelTasks(this);
        checkschedule.removeRunnable();
    }

    public static NoMoreESP getInstance() {
        return INSTANCE;
    }
    
    //log system

    public void log(String str, Object... args) {
        String message = String.format(str, args);
        if(Config.LOG_IN_FILE.getBoolean()){
            logInToFile(message, false);
        }
        if (Config.LOG_IN_CONSOLE.getBoolean()) {
            getLogger().info(message);
        }
    }

    public void logDebug(String str, Object... args) {
        String message = String.format(str, args) + " (DEBUG)";
        if(Config.DEBUG_IN_FILE.getBoolean()){
            logInToFile(message, true);
        }
        if (Config.DEBUG_IN_CONSOLE.getBoolean()) {
            getLogger().info(message);
        }
    }
    
    private void logInToFile(String msg, Boolean debug){
        new Thread(() -> {
            try
            {
                DateFormat logFileFormat = new SimpleDateFormat("yyyy_MM_dd");

                //如果不是 DEBUG
                if(!debug) {
                    File saveTo = new File(logFolder, logFileFormat + "_log.log");
                    if (!saveTo.exists()){
                        saveTo.createNewFile();
                    }
                    writeToFile(msg, saveTo);
                }

                //如果有開 debug 則全部紀錄 (含基本 log)
                if(Config.DEBUG_IN_FILE.getBoolean()){
                    File saveToDebug = new File(logFolder, logFileFormat + "_debug.log");
                    if (!saveToDebug.exists()){
                        saveToDebug.createNewFile();
                    }
                    writeToFile(msg, saveToDebug);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void writeToFile(String msg, File file) throws IOException{
        FileWriter fw = new FileWriter(file, true);
        PrintWriter pw = new PrintWriter(fw);
        DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ");
        Calendar cal = Calendar.getInstance();
        pw.println(dateFormat.format(cal.getTime()) + msg);
        pw.flush();
        pw.close();
    }
}
