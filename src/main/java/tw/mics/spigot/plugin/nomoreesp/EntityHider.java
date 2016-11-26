package tw.mics.spigot.plugin.nomoreesp;

import static com.comphenix.protocol.PacketType.Play.Server.ANIMATION;
import static com.comphenix.protocol.PacketType.Play.Server.ATTACH_ENTITY;
import static com.comphenix.protocol.PacketType.Play.Server.BED;
import static com.comphenix.protocol.PacketType.Play.Server.BLOCK_BREAK_ANIMATION;
import static com.comphenix.protocol.PacketType.Play.Server.COLLECT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_DESTROY;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EQUIPMENT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_HEAD_ROTATION;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_LOOK;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_STATUS;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_TELEPORT;
import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_VELOCITY;
import static com.comphenix.protocol.PacketType.Play.Server.MOUNT;
import static com.comphenix.protocol.PacketType.Play.Server.NAMED_ENTITY_SPAWN;
import static com.comphenix.protocol.PacketType.Play.Server.REL_ENTITY_MOVE;
import static com.comphenix.protocol.PacketType.Play.Server.REMOVE_ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_LIVING;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_PAINTING;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;

public class EntityHider implements Listener {
    private NoMoreESP plugin;
    private ProtocolManager manager;
    private PacketAdapter protocolListener;
    private HashMap<String, Set<Integer>> hiddenEntityPerPlayer;
    
    public EntityHider(NoMoreESP instance){
        this.plugin = instance;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        
        //Load ProtocolLib
        this.manager = ProtocolLibrary.getProtocolManager(); 
        
        //Init hiddenEntity
        hiddenEntityPerPlayer = new HashMap<String, Set<Integer>>();
        manager.addPacketListener(
                protocolListener = constructProtocol(plugin));
    }
    
    /**
     * Allow the observer to see an entity that was previously hidden.
     * @param observer - the observer.
     * @param entity - the entity to show.
     * @return TRUE if the entity was hidden before, FALSE otherwise.
     */
    public final boolean showEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean hiddenBefore = !setVisibility(observer, entity.getEntityId(), true);
        //plugin.log("%s can see %s now", observer.getName(), entity.getName());
        if( entity.isDead() ) {
            removeEntity(entity);
            return hiddenBefore;
        }
        // Resend packets
        if (manager != null && hiddenBefore) {
            try{
                manager.updateEntity(entity, Arrays.asList(observer));
            } catch (IllegalArgumentException e){
                
            } finally {
                if(entity.isInsideVehicle()){
                    showEntity(observer, entity.getVehicle());
                    // Make the entity mount on Vehicle
                    PacketContainer mountEntity = new PacketContainer(MOUNT);
                    mountEntity.getIntegers().write(0, entity.getVehicle().getEntityId());
                    mountEntity.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
                    try {
                        manager.sendServerPacket(observer, mountEntity);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Cannot send server packet.", e);
                    }
                }
            }
        }
        return hiddenBefore;
    }
    
    /**
     * Prevent the observer from seeing a given entity.
     * @param observer - the player observer.
     * @param entity - the entity to hide.
     * @return TRUE if the entity was previously visible, FALSE otherwise.
     */
    public final boolean hideEntity(Player observer, Entity entity) {
        validate(observer, entity);
        boolean visibleBefore = setVisibility(observer, entity.getEntityId(), false);
        //plugin.log("%s can't see %s now", observer.getName(), entity.getName());
        
        if (visibleBefore) {
            PacketContainer destroyEntity = new PacketContainer(ENTITY_DESTROY);
            destroyEntity.getIntegerArrays().write(0, new int[] { entity.getEntityId() });
            
            // Make the entity disappear
            try {
                manager.sendServerPacket(observer, destroyEntity);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot send server packet.", e);
            }
        }
        return visibleBefore;
    }

    public boolean isVisible(Player player, Entity entity) {
        return isVisible(player, entity.getEntityId());
    }
    

    public void close() {
        if (manager != null) {
            HandlerList.unregisterAll(this);
            manager.removePacketListener(protocolListener);
            manager = null;
        }
    }
    
    /**
     * Set the visibility status of a given entity for a particular observer.
     * @param observer - the observer player.
     * @param entity - ID of the entity that will be hidden or made visible.
     * @param visible - TRUE if the entity should be made visible, FALSE if not.
     * @return TRUE if the entity was visible before this method call, FALSE otherwise.
     */
    private boolean setVisibility(Player observer, int entityID, boolean visible) {
        Set<Integer> hiddenEntity = getHiddenEntity(observer);
        if(hiddenEntity.contains(entityID)){
            if(visible == true){
                hiddenEntity.remove((Object)entityID);
            }
            return false;
        } else {
            if(visible == false){
                hiddenEntity.add(entityID);
            }
            return true;
        }
    }
    
    private Set<Integer> getHiddenEntity(Player p){
        Set<Integer> hiddenEntity = null;
        try {
            hiddenEntity = hiddenEntityPerPlayer.get(p.getUniqueId().toString());
        } catch (UnsupportedOperationException e){
            return null;
        }
        if(hiddenEntity == null){
            hiddenEntity = Collections.newSetFromMap(new LinkedHashMap<Integer, Boolean>(){
                private static final long serialVersionUID = -2512724413724781814L;
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, Boolean> eldest) {
                    return size() > 500;
                }
            });
            hiddenEntityPerPlayer.put(p.getUniqueId().toString(), hiddenEntity);
        }
        return hiddenEntity;
    }
    
    private boolean isVisible(Player player, int entityID) {
        Set<Integer> hiddenEntity = getHiddenEntity(player);
        if (hiddenEntity == null){
            return true;
        } else if(hiddenEntity.contains(entityID)){
            return false;
        }
        return true;
    }
    
    // For valdiating the input parameters
    private void validate(Player observer, Entity entity) {
        Preconditions.checkNotNull(observer, "observer cannot be NULL.");
        Preconditions.checkNotNull(entity, "entity cannot be NULL.");
    }
    
    // ==================== EVENTS ==================== 
    @EventHandler
    private void onEntityDeath(EntityDeathEvent e) {
        removeEntity(e.getEntity());
    }
    
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        hiddenEntityPerPlayer.remove(e.getPlayer().getUniqueId().toString());
        removeEntity(e.getPlayer());
    }
    
    private void removeEntity(Entity entity){
        hiddenEntityPerPlayer.forEach((k,hideEntities)->{
            Iterator<Integer> iter = hideEntities.iterator();
            while (iter.hasNext()) {
                Integer hideEntityId = iter.next();

                if (hideEntityId == entity.getEntityId())
                    iter.remove();
            }
        });
    }
    
    // Packets that update remote player entities
    private static final PacketType[] ENTITY_PACKETS = { 
        ENTITY_EQUIPMENT, BED, ANIMATION, NAMED_ENTITY_SPAWN, 
        COLLECT, SPAWN_ENTITY, SPAWN_ENTITY_LIVING, SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_EXPERIENCE_ORB, 
        ENTITY_VELOCITY, REL_ENTITY_MOVE, ENTITY_LOOK, 
        ENTITY_TELEPORT, ENTITY_HEAD_ROTATION, ENTITY_STATUS, ATTACH_ENTITY, ENTITY_METADATA,
        ENTITY_EFFECT, REMOVE_ENTITY_EFFECT, BLOCK_BREAK_ANIMATION
        
        // We don't handle DESTROY_ENTITY though
    };
    
    // Listen PacketSending
    private PacketAdapter constructProtocol(Plugin plugin) {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityID = event.getPacket().getIntegers().read(0);
                
                // See if this packet should be cancelled
                if (!isVisible(event.getPlayer(), entityID)) {
                    event.setCancelled(true);
                }
            }

            
        };
    }
}
