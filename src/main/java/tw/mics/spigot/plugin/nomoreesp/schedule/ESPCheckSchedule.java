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
		final int PLAYER_TRACKING_RANGE = 48; 	//TODO get server setting
		final double DONT_HIDE_RANGE = 3;		//兩邊都會有作用 也就是實際距離是兩倍
		final double VECTOR_LENGTH = 0.5;
		for(Player player : plugin.getServer().getOnlinePlayers()){
			plugin.getServer().getOnlinePlayers().forEach(target -> {
				//plugin.log("p:%s t:%s", player.getName(), target.getName());
				Location loc = player.getLocation().add(0, 1.625 , 0);
				Location target_loc = target.getLocation().add(0, 1, 0);
				double distance = loc.distance(target_loc);
				double checked_distance = 0;
				if(distance > PLAYER_TRACKING_RANGE) return; 	    	// 過遠不用判斷
				if(player.equals(target)) return;						// 自己不用判斷
				if(distance < DONT_HIDE_RANGE){ 						// 過近直接不隱藏
					plugin.hider.showEntity(player, target);
					return;		
				}

				
				Vector vector1 = target_loc.subtract(loc).toVector();
				vector1.multiply(VECTOR_LENGTH/vector1.length());

				checked_distance +=  DONT_HIDE_RANGE;
				loc.add(vector1.clone().multiply(DONT_HIDE_RANGE / VECTOR_LENGTH)) ;

				while(checked_distance < distance - DONT_HIDE_RANGE){
					if(
						loc.getBlock().getType().isSolid() &&
						loc.clone().add(0,0.5,0).getBlock().getType().isSolid() &&
						loc.clone().add(0,-0.5,0).getBlock().getType().isSolid()
					){
						plugin.hider.hideEntity(player, target);
						return;
					}
					
					checked_distance += VECTOR_LENGTH;
					loc.add(vector1);
				}
				plugin.hider.showEntity(player, target);
			});
		}
	}

	public void removeRunnable(){
		this.plugin.getServer().getScheduler().cancelTask(schedule_id);
	}
}
