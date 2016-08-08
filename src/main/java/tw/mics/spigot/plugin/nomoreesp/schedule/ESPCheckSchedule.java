package tw.mics.spigot.plugin.nomoreesp.schedule;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import tw.mics.spigot.plugin.nomoreesp.Config;
import tw.mics.spigot.plugin.nomoreesp.EntityHider;
import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;

public class ESPCheckSchedule {
    NoMoreESP plugin;
    Runnable runnable;
    EntityHider hider;
    int schedule_id;

    // CONSTANT
    int PLAYER_TRACKING_RANGE = Config.CHECK_DISTANCE.getInt();
    int CHECK_TICK = Config.CHECK_TICK.getInt();

    final double DONT_HIDE_RANGE = 1.5; // 兩邊都會有作用 也就是實際距離是兩倍
    final double VECTOR_LENGTH = 0.8; // 每次增加確認的長
    final double HIDE_DEGREES = 70; // 此角度外則隱藏

    public ESPCheckSchedule(NoMoreESP i) {
        plugin = i;
        this.hider = new EntityHider(plugin);
        setupRunnable();
    }

    private void setupRunnable() {
        runnable = new Runnable() {
            public void run() {
                checkHide();
            }
        };
        schedule_id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, runnable, 0,
                CHECK_TICK);
    }

    protected void checkHide() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if(Config.ONLY_PLAYER.getBoolean()){
                List<Player> otherPlayers = new ArrayList<Player>(plugin.getServer().getOnlinePlayers());
                otherPlayers.remove(player);
                otherPlayers.forEach(target -> {
                    if(player.getWorld() == target.getWorld())
                        checkLookable(player, target);
                });
            } else {
                List<Entity> nearbyEntities = player.getNearbyEntities(PLAYER_TRACKING_RANGE * 2,
                        player.getWorld().getMaxHeight(), PLAYER_TRACKING_RANGE * 2);
                nearbyEntities.forEach(target -> {
                    if(target.getType().isAlive())
                        checkLookable(player, target);
                });
            }
        }
    }

    private void checkLookable(Player player, Entity target) {
        
        Location loc = player.getLocation().add(0, 1.125, 0); //1.625 - 0.5
        Location target_loc = target.getLocation().add(0, 0.5, 0); // 1 - 0.5
        
        double distance = loc.distance(target_loc);
        double checked_distance = 0;
        
        if(distance > PLAYER_TRACKING_RANGE) return;

        // too near, force show
        if (distance < DONT_HIDE_RANGE * 2) {
            hider.showEntity(player, target);
            return;
        }

        Vector vector1 = target_loc.subtract(loc).toVector();
        vector1.multiply(1 / vector1.length());

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
        loc.add(vector1.clone().multiply(DONT_HIDE_RANGE / VECTOR_LENGTH));

        // 判斷是否被方塊擋住
        vector1.multiply(VECTOR_LENGTH);
        distance -= DONT_HIDE_RANGE; // don't check if too near target
        while (checked_distance < distance) {
            if (
                    isOccluding(loc.getBlock().getType()) && 
                    isOccluding(loc.add(0, 1, 0).getBlock().getType())
                ) {
                hider.hideEntity(player, target);
                return;
            }
            checked_distance += VECTOR_LENGTH;
            loc.add(vector1);
        }
        hider.showEntity(player, target);
    }

    public void removeRunnable() {
        this.plugin.getServer().getScheduler().cancelTask(schedule_id);
    }
    
    //same as Material, but remove barrier
    public boolean isOccluding(Material m) {
        if (!m.isBlock()) {
            return false;
        }
        switch (m) {
            case STONE:
            case GRASS:
            case DIRT:
            case COBBLESTONE:
            case WOOD:
            case BEDROCK:
            case SAND:
            case GRAVEL:
            case GOLD_ORE:
            case IRON_ORE:
            case COAL_ORE:
            case LOG:
            case SPONGE:
            case LAPIS_ORE:
            case LAPIS_BLOCK:
            case DISPENSER:
            case SANDSTONE:
            case NOTE_BLOCK:
            case WOOL:
            case GOLD_BLOCK:
            case IRON_BLOCK:
            case DOUBLE_STEP:
            case BRICK:
            case BOOKSHELF:
            case MOSSY_COBBLESTONE:
            case OBSIDIAN:
            case MOB_SPAWNER:
            case DIAMOND_ORE:
            case DIAMOND_BLOCK:
            case WORKBENCH:
            case FURNACE:
            case BURNING_FURNACE:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
            case SNOW_BLOCK:
            case CLAY:
            case JUKEBOX:
            case PUMPKIN:
            case NETHERRACK:
            case SOUL_SAND:
            case JACK_O_LANTERN:
            case MONSTER_EGGS:
            case SMOOTH_BRICK:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case MELON_BLOCK:
            case MYCEL:
            case NETHER_BRICK:
            case ENDER_STONE:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case WOOD_DOUBLE_STEP:
            case EMERALD_ORE:
            case EMERALD_BLOCK:
            case COMMAND:
            case QUARTZ_ORE:
            case QUARTZ_BLOCK:
            case DROPPER:
            case STAINED_CLAY:
            case HAY_BLOCK:
            case HARD_CLAY:
            case COAL_BLOCK:
            case LOG_2:
            case PACKED_ICE:
            case SLIME_BLOCK:
            case PRISMARINE:
            case RED_SANDSTONE:
            case DOUBLE_STONE_SLAB2:
            case PURPUR_BLOCK:
            case PURPUR_PILLAR:
            case PURPUR_DOUBLE_SLAB:
            case END_BRICKS:
            case STRUCTURE_BLOCK:
            case COMMAND_REPEATING:
            case COMMAND_CHAIN:
            case MAGMA:
            case NETHER_WART_BLOCK:
            case RED_NETHER_BRICK:
            case BONE_BLOCK:
                return true;
            default:
                return false;
        }
    }
}
