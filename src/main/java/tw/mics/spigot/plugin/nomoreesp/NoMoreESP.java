package tw.mics.spigot.plugin.nomoreesp;

import org.bukkit.plugin.java.JavaPlugin;
import tw.mics.spigot.plugin.nomoreesp.schedule.ESPCheckSchedule;

public class NoMoreESP extends JavaPlugin {
    private static NoMoreESP INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		new ESPCheckSchedule(this);
    }

	public static NoMoreESP getInstance() {
		return INSTANCE;
	}
    
}
