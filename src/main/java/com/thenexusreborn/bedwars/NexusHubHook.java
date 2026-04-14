package com.thenexusreborn.bedwars;

import com.thenexusreborn.hub.NexusHub;
import com.thenexusreborn.hub.api.ServerSelectEvent;
import com.thenexusreborn.nexuscore.util.MsgType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class NexusHubHook implements Listener {
    private final NexusBedWarsPlugin bedWars;
    private final NexusHub nexusHub;
    
    public NexusHubHook(NexusBedWarsPlugin bedWars, Plugin nexusHubPlugin) {
        this.bedWars = bedWars;
        this.nexusHub = (NexusHub) nexusHubPlugin;
    }
    
    @EventHandler
    public void onServerSelect(ServerSelectEvent e) {
        String serverName = e.getServerName();
        
        if (e.getNexusPlayer() == null) {
            bedWars.getLogger().severe("NexusPlayer in a ServerSelectEvent was null.");
            UUID uuid = e.getUuid();
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                MsgType.ERROR.send(player, "There was an error while processing your server selection. Please leave and rejoin.");
                bedWars.getLogger().severe("  The player was online: " + player.getName());
            }
            
            return;
        }
        
        for (BWVirtualServer server : bedWars.getServers().values()) {
            if (server.getName().equalsIgnoreCase(serverName)) {
                server.join(e.getNexusPlayer());
                break;
            }
        }
    }
}