package tw.mics.spigot.plugin.nomoreesp.listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;
import tw.mics.spigot.plugin.nomoreesp.XRayDetect;

public class XRayDetectListener extends MyListener {
	public XRayDetectListener(NoMoreESP instance)
	{
	    super(instance);
	}

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event){
        Block b = event.getBlock();
        XRayDetect.playerBreakBlock(event.getPlayer().getUniqueId(), b, b.getType());
    }
}
