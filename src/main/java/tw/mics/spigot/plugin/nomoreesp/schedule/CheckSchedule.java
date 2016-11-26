package tw.mics.spigot.plugin.nomoreesp.schedule;

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import tw.mics.spigot.plugin.nomoreesp.Config;
import tw.mics.spigot.plugin.nomoreesp.EntityHider;
import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;
import tw.mics.spigot.plugin.nomoreesp.runnable.CheckHideEntityRunnable;
import tw.mics.spigot.plugin.nomoreesp.runnable.CheckXRayRunnable;

public class CheckSchedule {
    NoMoreESP plugin;
    Runnable runnable;
    public EntityHider hider;
    int schedule_id;
    private HashSet<EntityType> hide_list;

    public CheckSchedule(NoMoreESP i) {
        plugin = i;
        this.hider = new EntityHider(plugin);
        //load config
        hide_list = new HashSet<EntityType>();
        for(String type : Config.HIDE_ENTITY_HIDE_LIST.getStringList()){
            hide_list.add(EntityType.valueOf(type));
        }
        
        setupRunnable();
    }

    private void setupRunnable() {
        runnable = new Runnable() {
            public void run() {
                checkHide();
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0, 2);
    }

    protected void checkHide() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!Config.HIDE_ENTITY_ENABLE_WORLDS.getStringList().contains(player.getWorld().getName())){
                continue;
            }
            
            //hideentity
            if(Config.HIDE_ENTITY_ENABLE.getBoolean()){
                List<Entity> nearbyEntities = player.getNearbyEntities(Config.HIDE_ENTITY_HIDE_RANGE.getInt() * 2,
                        player.getWorld().getMaxHeight(), Config.HIDE_ENTITY_HIDE_RANGE.getInt() * 2);
                
                nearbyEntities.forEach(target -> {
                    if(hide_list.contains(target.getType()) && target != player)
                        checkLookable(player, target);
                });
            }
            
            //xray
            if(Config.XRAY_DETECT_ENABLE.getBoolean())
                new Thread(new CheckXRayRunnable(player)).start();
        }
    }

    private void checkLookable(Player player, Entity target) {
        new Thread(new CheckHideEntityRunnable(hider, player, target)).start();
        

    }

    public void removeRunnable() {
        this.plugin.getServer().getScheduler().cancelTask(schedule_id);
    }
}
