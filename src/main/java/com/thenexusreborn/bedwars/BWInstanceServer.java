package com.thenexusreborn.bedwars;

import com.thenexusreborn.api.player.NexusPlayer;
import com.thenexusreborn.api.server.InstanceServer;

public class BWInstanceServer extends InstanceServer {
    public BWInstanceServer(NexusBedWarsPlugin bedwars, String name) {
        super(name, "bedwars", 32);
        BWVirtualServer primaryServer = new BWVirtualServer(bedwars, this, "BW1");
        this.primaryVirtualServer.set(primaryServer);
    }
    
    @Override
    public void join(NexusPlayer player) {
        this.primaryVirtualServer.get().join(player);
    }
    
    @Override
    public void quit(NexusPlayer player) {
        this.primaryVirtualServer.get().quit(player);
    }
    
    @Override
    public void onStart() {
        this.primaryVirtualServer.get().onStart();
    }
    
    @Override
    public void onStop() {
        this.primaryVirtualServer.get().onStop();
    }
}
