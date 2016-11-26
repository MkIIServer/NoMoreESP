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
    final double HIDE_DEGREES = 60;    // 此角度外則隱藏
    
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
        Vector vector = target_loc.subtract(loc).toVector();
        
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
        
        // 在視角外則隱藏
        Vector A = vector;
        Vector B = loc.getDirection();
        double degrees = Math.toDegrees(Math.acos(B.dot(A)/vector.length()));
        if (degrees > HIDE_DEGREES) {
            hider.hideEntity(player, target);
            return;
        }

        
        // 判斷是否被方塊擋住
        checked_distance += vector.length();
        loc.add(vector);
        while (checked_distance < distance) {
            //player.getWorld().spawnParticle(Particle.DRIP_LAVA, loc.getX(), loc.getY(), loc.getZ(), 1, 0, 0, 0);
            if (loc.getBlock().getType().isOccluding()) {
                hider.hideEntity(player, target);
                return;
            }
            checked_distance += vector.length();
            loc.add(vector);
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
