package tw.mics.spigot.plugin.nomoreesp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

import tw.mics.spigot.plugin.cupboard.CupboardAPI;

public class XRayDetect {
    static HashMap<UUID, LinkedHashMap<Block,Double>> player_break_block_add_vl;
    static HashMap<UUID, HashSet<Block>> player_breaked_block;
    static HashMap<UUID, Double> player_vl;
    static HashMap<Material, Double> config_block_value;
    public static void initData(){
        player_break_block_add_vl = new HashMap<UUID, LinkedHashMap<Block,Double>>();
        player_breaked_block = new HashMap<UUID, HashSet<Block>>();
        player_vl = new HashMap<UUID, Double>();
        config_block_value = new HashMap<Material, Double>();
        
        for(String str : Config.XRAY_DETECT_ADD_VL_BLOCK_AND_NUMBER.getStringList()){
            String[] str_split = str.split(":");
            String material = str_split[0];
            String value = str_split[1];
            config_block_value.put(Material.valueOf(material), Double.valueOf(value));
        }
    }

    public static void checkUUIDDataExist(UUID player){
        int maxEntries = 1000;
        if(!player_break_block_add_vl.containsKey(player)){
            player_break_block_add_vl.put(player,new LinkedHashMap<Block,Double>(maxEntries*10/7, 0.7f, true){
                private static final long serialVersionUID = 7122398289557675273L;
                @Override
                protected boolean removeEldestEntry(Map.Entry<Block, Double> eldest) {
                    return size() > maxEntries;
                }
            });
        }
        if(!player_breaked_block.containsKey(player)){
            player_breaked_block.put(player,new HashSet<Block>());
        }
        if(!player_vl.containsKey(player)){
            player_vl.put(player, 0.0);
        }
    }
    
    public static void removeUUID(UUID player){
        player_break_block_add_vl.remove(player);
        player_breaked_block.remove(player);
        player_vl.remove(player);
    }
    
    public static LinkedHashMap<Block, Double> getBreakAddVL(UUID player){
        checkUUIDDataExist(player);
        return player_break_block_add_vl.get(player);
    }
    
    public static HashMap<Material, Double> getBlockValue(){
        return config_block_value;
    }

    public static void playerBreakBlock(UUID player, Block block) {
        
        new Thread(new Runnable(){
            @Override
            public void run() {
                checkUUIDDataExist(player);
                Double vl = player_break_block_add_vl.get(player).get(block);
                HashSet<Block> breaked_block = player_breaked_block.get(player);
                
                //如果有值
                if(vl != null){        
                    if(breaked_block.contains(block)) return;
                    breaked_block.add(block);
                    boolean limit;
                    try {
                        Class.forName("tw.mics.spigot.plugin.cupboard.CupboardAPI");
                        limit  = CupboardAPI.checkIsLimit(block);
                    } catch (ClassNotFoundException e) {
                        limit = false;
                    }
                    if(limit)return;
                    
                    player_vl.put(player, player_vl.get(player) + vl);
                    
                    //reach vl and run command
                    if(player_vl.get(player) > Config.XRAY_DETECT_RUN_COMMAND_VL.getInt()){
                        //log
                        NoMoreESP.getInstance().logInToFile("UUID " + Bukkit.getPlayer(player).getName() +
                                " is reach command vl (now vl is " + player_vl.get(player) + ")" );
                        
                        //run command
                        String str = Config.XRAY_DETECT_RUN_COMMAND.getString().replace("%PLAYER%", Bukkit.getPlayer(player).getName());
                        if(!str.isEmpty()){
                            Bukkit.getScheduler().scheduleSyncDelayedTask(NoMoreESP.getInstance(), new Runnable(){
                                @Override
                                public void run() {
                                     Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), str);
                                }
                            });
                        }
                        
                        //reset vl
                        player_vl.put(player, 0.0);
                    }
                } else if(player_vl.get(player) > 0) {
                    player_vl.put(player, player_vl.get(player) - 1);
                } else {
                    return;
                }
                
                //debug message
                if(Config.DEBUG.getBoolean()){
                    NoMoreESP.getInstance().logDebugInToFile(Bukkit.getPlayer(player).getName() + "'s VL is now " + player_vl.get(player));
                }
            }
        }).start();
    }
}
