package tw.mics.spigot.plugin.nomoreesp;

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA;
import static com.comphenix.protocol.PacketType.Play.Server.NAMED_ENTITY_SPAWN;
import static com.comphenix.protocol.PacketType.Play.Server.SPAWN_ENTITY_LIVING;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class HealthHider implements Listener {
    private NoMoreESP plugin;
    private ProtocolManager manager;
    private PacketAdapter protocolListener;
    
    public HealthHider(NoMoreESP instance){
        this.plugin = instance;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        
        //Load ProtocolLib
        this.manager = ProtocolLibrary.getProtocolManager(); 
        
        //Init hiddenEntity
        manager.addPacketListener(
                protocolListener = constructProtocol(plugin));
    }
    
 // Packets that update remote player entities
    private static final PacketType[] ENTITY_PACKETS = { 
        SPAWN_ENTITY_LIVING, 
        NAMED_ENTITY_SPAWN,
        ENTITY_METADATA
    };
    
    // Listen PacketSending
    private PacketAdapter constructProtocol(Plugin plugin) {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Entity e = packet.getEntityModifier(event).read(0);
                if(     
                        e instanceof LivingEntity && 
                        packet.getWatchableCollectionModifier().read(0) != null &&
                        e.getType() != EntityType.HORSE &&
                        e.getType() != EntityType.PIG &&
                        e.getType() != EntityType.WOLF &&
                        e.getUniqueId() != event.getPlayer().getUniqueId()
                ){
                    event.setPacket(packet = packet.deepClone());
                    WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getWatchableCollectionModifier().read(0));
                    processDataWatcher(watcher);
                    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                }
            }
            
            private void processDataWatcher(WrappedDataWatcher watcher) {
                if(watcher != null && watcher.hasIndex(7) && watcher.getFloat(7) != 0){ // 2 is name index
                    watcher.setObject(7, Float.valueOf(1));
                }
            }
        };
    }
    
    public void close() {
        if (manager != null) {
            HandlerList.unregisterAll(this);
            manager.removePacketListener(protocolListener);
            manager = null;
        }
    }
}
