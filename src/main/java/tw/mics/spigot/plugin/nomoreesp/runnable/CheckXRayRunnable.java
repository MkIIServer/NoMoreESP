package tw.mics.spigot.plugin.nomoreesp.runnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.nomoreesp.Config;
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
            Double value = null;
            Biome biome = null;
            Material blocktype;
            
            try{
                blocktype = loc.getBlock().getType();
                biome = loc.getBlock().getBiome();
            } catch (IllegalStateException e){
                break; //skip when error
            }
            value = XRayDetect.getBlockValue().get(blocktype);
            if (value != null) {
                if(blocktype == Material.GOLD_ORE){
                    switch(biome){
                    case BADLANDS:
                    case ERODED_BADLANDS:
                    case WOODED_BADLANDS_PLATEAU:
                    case MODIFIED_BADLANDS_PLATEAU:
                    case MODIFIED_WOODED_BADLANDS_PLATEAU:
                    case BADLANDS_PLATEAU:
                        value /= Config.XRAY_DETECT_GOLD_VL_DIVIDED_NUMBER_IN_MESA.getDouble();
                    default:
                        break;
                    }
                }
                LinkedHashMap<Block, Double> block_value_set = XRayDetect.getBreakAddVL(player.getUniqueId());
                
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
