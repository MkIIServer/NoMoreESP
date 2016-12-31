package tw.mics.spigot.plugin.nomoreesp.schedule;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
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
    boolean keep_check;
    private HashSet<EntityType> hide_list;

    public CheckSchedule(NoMoreESP i) {
        plugin = i;
        keep_check = true;
        hider = new EntityHider(plugin);
        
        //load config
        hide_list = new HashSet<EntityType>();
        for(String type : Config.HIDE_ENTITY_HIDE_LIST.getStringList()){
            hide_list.add(EntityType.valueOf(type));
        }
        
        setupRunnable();
    }

    private void setupRunnable() {
        check();
    }

    protected void check() {
        Bukkit.getScheduler().runTaskAsynchronously(NoMoreESP.getInstance(), 
            new Runnable(){
                @Override
                public void run() {
                    try{
                        while(keep_check){
                            Iterator<? extends Player> iter_online_player = plugin.getServer().getOnlinePlayers().iterator();
                            while (iter_online_player.hasNext()) {
                                Player player = iter_online_player.next();
                                if(!player.isOnline() || !player.isValid())
                                    continue;
                                //hideentity
                                if(
                                        Config.HIDE_ENTITY_ENABLE.getBoolean() && 
                                        Config.HIDE_ENTITY_ENABLE_WORLDS.getStringList().contains(player.getWorld().getName())
                                ){
                                    //this shouldn't async
                                    List<Entity> nearbyEntities = player.getNearbyEntities(Config.HIDE_ENTITY_HIDE_RANGE.getInt() * 2,
                                            player.getWorld().getMaxHeight(), Config.HIDE_ENTITY_HIDE_RANGE.getInt() * 2);
                                    
                                    nearbyEntities.remove(player);
                                    nearbyEntities.forEach(target -> {
                                    if(hide_list.contains(target.getType()))
                                        Bukkit.getScheduler().runTaskAsynchronously(NoMoreESP.getInstance(), 
                                                new CheckHideEntityRunnable(hider, player, target));
                                    });
                                }
                                
                                //xray
                                if(
                                        Config.XRAY_DETECT_ENABLE.getBoolean() && 
                                        Config.XRAY_DETECT_ENABLE_WORLDS.getStringList().contains(player.getWorld().getName())
                                ){
                                    Bukkit.getScheduler().runTaskAsynchronously(NoMoreESP.getInstance(), 
                                            new CheckXRayRunnable(player));
                                }
                            }
                            Thread.sleep(200);
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    } finally {
                        try { Thread.sleep(200); } catch (InterruptedException e) {}
                        if(keep_check) check();
                    }
                }
            }
        );
    }

    public void removeRunnable() {
        keep_check = false;
    }
}
