package tw.mics.spigot.plugin.nomoreesp.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;
import tw.mics.spigot.plugin.nomoreesp.XRayDetect;

public class XRayDetectListener extends MyListener {
	public XRayDetectListener(NoMoreESP instance)
	{
	    super(instance);
	}

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        XRayDetect.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event){
        XRayDetect.playerBreakBlock(event.getPlayer(), event.getBlock());
    }
}
