package com.thenexusreborn.bedwars;

import com.stardevllc.minecraft.actors.Actors;
import com.stardevllc.starchat.rooms.ChatRoom;

public class GameTeamChatroom extends ChatRoom {

    private final GameTeam team;

    public GameTeamChatroom(NexusBedWarsPlugin plugin, BWVirtualServer server, GameTeam team) {
        super(plugin, Actors.of(plugin), "room-game-" + server.getName().toLowerCase().replace(" ", "_") + "-" + team.getName().toLowerCase());
        this.team = team;
        senderFormat.set(team.getChatColor() + "[" + team.getName().toUpperCase() + "] &8(&2&l%nexuscore_level%&8) &r%nexuscore_prefix% %nexuscore_coloredname%&8: %nexuscore_chatcolor%{message}");
        systemFormat.set("{message}");
        useColorPermissions.set(true);
    }
    
    public GameTeam getTeam() {
        return team;
    }
}
