package tw.mics.spigot.plugin.nomoreesp.runnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.nomoreesp.Config;
import tw.mics.spigot.plugin.nomoreesp.EntityHider;
import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;



public class CheckHideEntityRunnable implements Runnable {
    final double DONT_HIDE_RANGE = 4;  // 兩邊都會有作用 也就是實際距離是兩倍
    final double HIDE_DEGREES = 70;    // 此角度外則隱藏
    
    EntityHider hider;
    Player player;
    Entity target;
    Location loc;
    Location target_loc;
    
    public CheckHideEntityRunnable(EntityHider hider, Player player, Entity target) {
        this.hider = hider;
        this.player = player;
        this.target = target;
        this.loc = player.getLocation().add(0, 1.625, 0); //1.625
        this.target_loc = target.getLocation().add(0, 1, 0); // 1
    }
    
    @Override
    public void run() {
        double distance = loc.distance(target_loc);
        double checked_distance = 0;

        // too far, force hide
        if(distance > Config.HIDE_ENTITY_HIDE_RANGE.getInt()){
            hider.hideEntity(player, target);
            return;
        }
        
        // too near, force show
        if (distance < DONT_HIDE_RANGE * 2) {
            this.showEntity(player, target);
            return;
        }

        // 計算+1
        Vector vector1 = target_loc.subtract(loc).toVector();
        vector1.multiply(1 / vector1.length());
        double x = vector1.getX();
        double y = vector1.getY();
        double z = vector1.getZ();
        if( x > y && x > z){
            x = 1;
            y *= 1/x;
            z *= 1/x;
        } else if( y > x && y > z){
            x *= 1/y;
            y = 1;
            z *= 1/y;
        } else if( z > x && z > y){
            x *= 1/z;
            y *= 1/z;
            z = 1;
        }
        vector1 = new Vector(x, y, z);

        // 在視角外則隱藏
        Vector A = vector1.clone();
        Vector B = loc.getDirection();
        double degrees = Math.toDegrees(Math.acos(A.dot(B) / A.length() * B.length()));
        if (degrees > HIDE_DEGREES) {
            hider.hideEntity(player, target);
            return;
        }

        // don't check too near block
        checked_distance += DONT_HIDE_RANGE;
        loc.add(vector1.clone().add(vector1));

        // 判斷是否被方塊擋住
        distance -= DONT_HIDE_RANGE; // don't check if too near target
        while (checked_distance < distance) {
            //player.sendBlockChange(loc, Material.GLASS, (byte) 0);
            if(!loc.getChunk().isLoaded())return; //check loaded
            if (loc.getBlock().getType().isOccluding()) {
                hider.hideEntity(player, target);
                return;
            }
            checked_distance += vector1.length();
            loc.add(vector1);
        }
        this.showEntity(player, target);
    }
    private void showEntity(Player player, Entity target){
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(NoMoreESP.getInstance(), new Runnable(){
            @Override
            public void run() {
                hider.showEntity(player, target);
            }
        });
    }
}
