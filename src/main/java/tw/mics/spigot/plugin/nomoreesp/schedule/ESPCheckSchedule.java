package tw.mics.spigot.plugin.nomoreesp.schedule;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
		final int player_tracking_range = 48; //TODO get server setting
		final double DONT_HIDE_RANGE = 5;
		final double VECTOR_LENGTH = 0.5;
		for(Player player : plugin.getServer().getOnlinePlayers()){
			next_target:
			for(Player target : plugin.getServer().getOnlinePlayers()){
				Location loc = player.getLocation();
				Location target_loc = target.getLocation();
				double distance = loc.distance(target_loc);
				double checked_distance = 0;
				if(distance > player_tracking_range) break next_target; 	    // 過遠不用判斷
				if(player.equals(target)) break next_target;					// 自己不用判斷
				if(distance < DONT_HIDE_RANGE){ 								// 過近直接不隱藏
					plugin.hider.showEntity(player, target);
					break next_target;			
				}

				
				Vector vector1 = target_loc.subtract(loc).toVector();
				vector1.multiply(VECTOR_LENGTH/vector1.length());

				checked_distance +=  DONT_HIDE_RANGE;
				loc.add(vector1.clone().multiply(DONT_HIDE_RANGE / VECTOR_LENGTH)) ;

				while(checked_distance < distance){
					if(
						loc.getBlock().getType().isSolid() &&
						loc.clone().add(0,1,0).getBlock().getType().isSolid()
					){
						plugin.hider.hideEntity(player, target);
						break next_target;
					}
					
					checked_distance += VECTOR_LENGTH;
					loc.add(vector1);
					plugin.log(" %f %f ", checked_distance, distance);
				}
				plugin.hider.showEntity(player, target);
			}
		}
	}

	public void removeRunnable(){
		this.plugin.getServer().getScheduler().cancelTask(schedule_id);
	}
}
