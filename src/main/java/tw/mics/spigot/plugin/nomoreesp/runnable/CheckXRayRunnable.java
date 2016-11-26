package tw.mics.spigot.plugin.nomoreesp.runnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.nomoreesp.XRayDetect;

public class CheckXRayRunnable implements Runnable {
    Player player;
    
    public CheckXRayRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        //get spVector
        Location loc = player.getLocation();
        loc.add(0, 1.625, 0);
        Vector vector = loc.getDirection();
        HashSet<Block> blocks = new HashSet<Block>();
        double x = Math.abs(vector.getX());
        double y = Math.abs(vector.getY());
        double z = Math.abs(vector.getZ());
        if( x > y && x > z){
            vector.multiply(1/x);
        } else if( y > x && y > z){
            vector.multiply(1/y);
        } else {
            vector.multiply(1/z);
        }
        for(int i = 0; i < 20 ; i++) {
            loc.add(vector);
            Double value = XRayDetect.getBlockValue().get(loc.getBlock().getType());
            if (value != null) {
                LinkedHashMap<Block, Double> block_value_set = XRayDetect.getBreakAddVL(player);
                
                Iterator<Block> iter = blocks.iterator();
                while(iter.hasNext()){
                    Block block = iter.next();
                    block_value_set.put(block, value);
                }
                break;
            }
            blocks.add(loc.getBlock());
        }
    }

}
