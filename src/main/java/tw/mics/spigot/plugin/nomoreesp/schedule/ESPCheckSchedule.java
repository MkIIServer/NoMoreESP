package tw.mics.spigot.plugin.nomoreesp.schedule;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.nomoreesp.EntityHider;
import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;

public class ESPCheckSchedule {
    NoMoreESP plugin;
    Runnable runnable;
    EntityHider hider;
    int schedule_id;

    //CONSTANT
    final int PLAYER_TRACKING_RANGE = 64;   //TODO get server setting
    final int CHECK_TICK = 2;
    
    final double DONT_HIDE_RANGE = 1.5;     //兩邊都會有作用 也就是實際距離是兩倍
    final double VECTOR_LENGTH = 0.8;       //每次增加確認的長
    final double HIDE_DEGREES = 70;         //此角度外則隱藏
    
    public ESPCheckSchedule(NoMoreESP i){
        plugin = i;
        this.hider = new EntityHider(plugin);
        setupRunnable();
    }
    
    private void setupRunnable(){
        runnable = new Runnable(){
            public void run() {
                checkHide();
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, CHECK_TICK);
    }

    protected void checkHide() {
        for(Player player : plugin.getServer().getOnlinePlayers()){
            List<Entity> nearbyEntities = player.getNearbyEntities(PLAYER_TRACKING_RANGE, player.getWorld().getMaxHeight(), PLAYER_TRACKING_RANGE);
            nearbyEntities.forEach(target -> checkLookable(player, target));
            //plugin.getServer().getOnlinePlayers().forEach(target -> checkLookable(player, target));
            //player.getWorld().getEntities().forEach(target -> checkLookable(player, target));
        }
    }
    
    private void checkLookable(Player player, Entity target){
            //plugin.log("p:%s t:%s", player.getName(), target.getName());
            Location loc = player.getLocation().add(0, 1.625 , 0);
            Location target_loc = target.getLocation().add(0, 1, 0);
            
            double distance = loc.distance(target_loc);
            double checked_distance = 0;
            
            //if(distance > PLAYER_TRACKING_RANGE) return;          // too far, no need ((checked before function call
            //if(player.equals(target)) return;                     // don't need itself ((never call itself now
            if(!target.getType().isAlive()) return;                 // only check alive entity
            if(distance < DONT_HIDE_RANGE * 2){                     // too near, force show
                hider.showEntity(player, target);
                return;     
            }

            
            Vector vector1 = target_loc.subtract(loc).toVector();
            vector1.multiply(1/vector1.length());
            
            
            //在視角外則隱藏
            Vector A = vector1.clone();
            Vector B = loc.getDirection();
            double degrees = Math.toDegrees(Math.acos( A.dot(B) / A.length() * B.length() ));
            if(degrees > HIDE_DEGREES){
                hider.hideEntity(player, target);
                return;
            }
            
            //don't check too near block
            checked_distance +=  DONT_HIDE_RANGE;  
            loc.add(vector1.clone().multiply(DONT_HIDE_RANGE / VECTOR_LENGTH)) ;

            //判斷是否被方塊擋住
            vector1.multiply(VECTOR_LENGTH);
            distance -= DONT_HIDE_RANGE; //don't check if too near target
            while(checked_distance < distance){
                if(
                    loc.getBlock().getType().isOccluding() &&
                    loc.clone().add(0,0.5,0).getBlock().getType().isOccluding() &&
                    loc.clone().add(0,-0.5,0).getBlock().getType().isOccluding()
                ){
                    hider.hideEntity(player, target);
                    return;
                }
                checked_distance += VECTOR_LENGTH;
                loc.add(vector1);
            }
            hider.showEntity(player, target);
    }

    public void removeRunnable(){
        this.plugin.getServer().getScheduler().cancelTask(schedule_id);
    }
}
