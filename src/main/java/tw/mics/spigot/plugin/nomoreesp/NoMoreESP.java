package tw.mics.spigot.plugin.nomoreesp;

import org.bukkit.plugin.java.JavaPlugin;

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
    
    
}
