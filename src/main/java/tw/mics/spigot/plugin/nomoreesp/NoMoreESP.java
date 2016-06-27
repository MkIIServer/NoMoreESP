package tw.mics.spigot.plugin.nomoreesp;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.nomoreesp.command.TestCommand;
import tw.mics.spigot.plugin.nomoreesp.schedule.ESPCheckSchedule;

public class NoMoreESP extends JavaPlugin {
    private static NoMoreESP INSTANCE;
	public EntityHider hider;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		this.hider = new EntityHider(this);
		new ESPCheckSchedule(this);
        this.getCommand("test").setExecutor(new TestCommand(this));
    }
	
	public void reload(){
		
	}
    @Override
    public void onDisable() {
    	
    }

	public void log(String str, Object... args)
	{
		String message = String.format(str, args);
		getLogger().info(message);
	}

	public static NoMoreESP getInstance() {
		return INSTANCE;
	}
	
	public void checkBlock(Player player, Player target){
		int player_tracking_range = 48; //TODO get server setting
		int dont_hide_range = 5;
		double vector_length = 0.5;
		
		Location loc = player.getLocation().add(0, 1, 0);
		Location target_loc = target.getLocation().add(0, 0.5, 0);
		double distance = loc.distance(target_loc);
		
		double checked_distance = 0;
		if(distance < player_tracking_range){
			if(player == target) return;
			
			Vector vector1 = target_loc.subtract(loc).toVector();
			vector1.multiply(vector_length/vector1.length());

			checked_distance += vector_length * dont_hide_range ;
			loc.add(vector1.clone().multiply(dont_hide_range)) ;
			
			while(checked_distance < distance){
				if( new Random().nextDouble() <= 0.2 ){
					loc.getBlock().setType(Material.DIRT);
					loc.clone().add(0,1,0).getBlock().setType(Material.DIRT);
					break;
				}
				
				checked_distance += vector_length;
				loc.add(vector1);
			}
			this.hider.showEntity(player, target);
		}
	}
    
    
}
