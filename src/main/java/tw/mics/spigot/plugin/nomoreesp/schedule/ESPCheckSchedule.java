package tw.mics.spigot.plugin.nomoreesp.schedule;

import java.util.logging.Level;

import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;

public class ESPCheckSchedule {
	NoMoreESP plugin;
	Runnable runnable;
	int schedule_id;
	
	public ESPCheckSchedule(NoMoreESP i){
		plugin = i;
		setupRunnable();
	}
	
	private void setupRunnable(){
		runnable = new Runnable(){
			public void run() {
				checkHide();
			}
		};
		schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, 5);
	}
	
	protected void checkHide() {
		int player_tracking_range = plugin.getServer().spigot().getConfig().getInt("entity-tracking-range.players");
		plugin.getLogger().log(Level.INFO, String.format("%d", player_tracking_range));
		for(Player player : plugin.getServer().getOnlinePlayers()){
			for(Player target : plugin.getServer().getOnlinePlayers()){
				if(player.getLocation().distance(target.getLocation()) < player_tracking_range){
					//do something :)
				}
			}
		}
	}

	public void removeRunnable(){
		this.plugin.getServer().getScheduler().cancelTask(schedule_id);
	}
}
