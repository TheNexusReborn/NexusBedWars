package com.thenexusreborn.bedwars.team;

import com.stardevllc.minecraft.actors.Actors;
import com.stardevllc.starchat.rooms.ChatRoom;
import com.thenexusreborn.bedwars.NexusBedWarsPlugin;
import com.thenexusreborn.bedwars.arena.BedwarsArena;
import com.thenexusreborn.bedwars.server.BWVirtualServer;

public class TeamChatroom extends ChatRoom {

    private final GameTeam team;

    public TeamChatroom(NexusBedWarsPlugin plugin, BWVirtualServer server, GameTeam team) {
        super(plugin, Actors.of(plugin), "room-" + server.getName().toLowerCase().replace(" ", "_") + "-" + team.getName().toLowerCase());
        this.team = team;
        senderFormat.set(team.getChatColor() + "[" + team.getName().toUpperCase() + "] &8(&2&l%nexuscore_level%&8) &r%nexuscore_displayname%&8: %nexuscore_chatcolor%{message}");
        systemFormat.set("{message}");
        useColorPermissions.set(true);
    }
    
    public TeamChatroom(NexusBedWarsPlugin plugin, BWVirtualServer server, GameTeam team, BedwarsArena arena) {
        super(plugin, Actors.of(plugin), "room-" + server.getName().toLowerCase().replace(" ", "_") + "-" + arena.getName().toLowerCase().replace(" ", "_") + team.getName());
        this.team = team;
        senderFormat.set(team.getChatColor() + "[" + team.getName().toUpperCase() + "] &8(&2&l%nexuscore_level%&8) &r%nexuscore_displayname%&8: %nexuscore_chatcolor%{message}");
        systemFormat.set("{message}");
        useColorPermissions.set(true);
    }
    
    public GameTeam getTeam() {
        return team;
    }
}
