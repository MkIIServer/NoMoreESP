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

import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;
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
            Block value_block = null;
            Double value = null;
            String block_location_string = null;
            Material blocktype;
            
            try{
                value_block = loc.getBlock();
                blocktype = value_block.getType();
                block_location_string = value_block.getX() + ", " + value_block.getY() + ", " + value_block.getZ();
            } catch (IllegalStateException e){
                break; //skip when error
            }
            value = XRayDetect.getBlockValue(blocktype);
            if (value != null) { //如果指到有價值的方塊
                LinkedHashMap<Block, HashSet<Block>> value_block_count_block_set = XRayDetect.getValueBlockCountBlockSet(player.getUniqueId());
                
                HashSet<Block> count_block_set = value_block_count_block_set.get(value_block);
                if(count_block_set == null){
                    count_block_set = new HashSet<Block>();
                    value_block_count_block_set.put(value_block, count_block_set);
                }

                Iterator<Block> iter = blocks.iterator();
                while(iter.hasNext()){
                    Block count_block = iter.next();
                    if(count_block.getType().isSolid()){ //實體方塊才算
                        count_block_set.add(count_block);
                    }
                }
                NoMoreESP.getInstance().logDebug( "%s look at value block(%s) now have %d count blocks.", player.getName(), block_location_string, count_block_set.size());
                break;
            }
            blocks.add(loc.getBlock());
        }
    }

}
